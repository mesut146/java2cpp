package com.mesut.j2cpp.visitor;

import com.mesut.j2cpp.Config;
import com.mesut.j2cpp.LibImplHandler;
import com.mesut.j2cpp.Logger;
import com.mesut.j2cpp.Mapper;
import com.mesut.j2cpp.ast.*;
import com.mesut.j2cpp.cppast.*;
import com.mesut.j2cpp.cppast.expr.*;
import com.mesut.j2cpp.cppast.literal.*;
import com.mesut.j2cpp.cppast.stmt.*;
import com.mesut.j2cpp.util.ArrayHelper;
import com.mesut.j2cpp.util.BindingMap;
import com.mesut.j2cpp.util.TypeHelper;
import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

@SuppressWarnings("unchecked")
public class SourceVisitor extends DefaultVisitor<CNode, CNode> {

    CSource source;
    TypeVisitor typeVisitor;
    CClass clazz;
    CMethod method;
    Catcher catcher;
    ITypeBinding binding;
    Mapper mapper;

    public SourceVisitor(CSource source) {
        this.source = source;
        this.typeVisitor = new TypeVisitor(source.header);
    }

    public TypeVisitor getTypeVisitor() {
        return typeVisitor;
    }

    public CHeader getHeader() {
        return source.header;
    }

    public void convert() {
        for (CClass clazz : source.header.classes) {
            convertClass(clazz);
        }
    }

    private void convertClass(CClass clazz) {
        this.clazz = clazz;
        for (CField field : clazz.fields) {
            if (field.isStatic() && field.expression != null) {
                //normal static field or enum constant
                source.fieldDefs.add(field);
            }
        }
        for (CMethod methodDecl : clazz.methods) {
            if (methodDecl.body != null) {
                source.methods.add(methodDecl);
            }
        }
        for (CClass inner : clazz.classes) {
            convertClass(inner);
        }
    }

    @Override
    public CNode visit(Block n, CNode arg) {
        CBlockStatement res = new CBlockStatement();
        for (Statement statement : (List<Statement>) n.statements()) {
            CStatement cStatement = (CStatement) visitExpr(statement, arg);
            if (!(cStatement instanceof CEmptyStatement)) {
                res.addStatement(cStatement);
            }
        }
        return res;
    }

    @Override
    public CNode visit(TypeDeclarationStatement node, CNode arg) {
        TypeDeclaration typeDeclaration = (TypeDeclaration) node.getDeclaration();
        DeclarationVisitor visitor = new DeclarationVisitor(this);
        return visitor.visit(typeDeclaration, null);
    }

    @Override
    public CNode visit(AssertStatement node, CNode arg) {
        return new CEmptyStatement();
    }


    @Override
    public CNode visit(StringLiteral node, CNode arg) {
        CObjectCreation objectCreation = new CObjectCreation();
        objectCreation.type = TypeHelper.getStringType();
        objectCreation.args.add(new CStringLiteral(node.getLiteralValue(), node.getEscapedValue()));
        return objectCreation;
    }

    @Override
    public CNode visit(BooleanLiteral node, CNode arg) {
        return new CBooleanLiteral(node.booleanValue());
    }

    @Override
    public CNode visit(CharacterLiteral node, CNode arg) {
        CCharacterLiteral characterLiteral = new CCharacterLiteral();
        characterLiteral.value = node.getEscapedValue();
        characterLiteral.charValue = node.charValue();
        return characterLiteral;
    }

    @Override
    public CNode visit(NullLiteral node, CNode arg) {
        return new CNullLiteral();
    }

    //type.class
    @Override
    public CNode visit(TypeLiteral node, CNode arg) {
        //Class::forName(type)
        CType type = typeVisitor.visitType(node.getType(), clazz);
        CType classType = TypeHelper.getClassType();
        type.scope = getHeader().source;
        classType.scope = getHeader().source;

        CMethodInvocation methodInvocation = new CMethodInvocation();
        methodInvocation.scope = new CName(classType.basicForm());
        methodInvocation.name = new CName("forName");
        methodInvocation.isArrow = false;
        methodInvocation.arguments.add(new CStringLiteral(type.basicForm(), null));
        return methodInvocation;
    }

    @Override
    public CNode visit(Assignment node, CNode arg) {
        CAssignment assignment = new CAssignment();
        assignment.left = (CExpression) visitExpr(node.getLeftHandSide(), arg);
        assignment.operator = node.getOperator().toString();
        assignment.right = (CExpression) visitExpr(node.getRightHandSide(), arg);
        return assignment;
    }

    @Override
    public CNode visit(BreakStatement node, CNode arg) {
        CBreakStatement breakStatement = new CBreakStatement();
        if (node.getLabel() != null) {
            breakStatement.label = node.getLabel().getIdentifier();
        }
        return breakStatement;
    }

    @Override
    public CNode visit(ContinueStatement node, CNode arg) {
        CContinueStatement continueStatement = new CContinueStatement();
        if (node.getLabel() != null) {
            continueStatement.label = node.getLabel().getIdentifier();
        }
        return continueStatement;
    }

    @Override
    public CNode visit(ReturnStatement node, CNode arg) {
        if (catcher != null) {
            return catcher.visit(node);
        }
        return new CReturnStatement((CExpression) visitExpr(node.getExpression(), arg));
    }

    @Override
    public CNode visit(ThrowStatement node, CNode arg) {
        if (catcher != null) {
            return catcher.visit(node);
        }
        return new CThrowStatement((CExpression) visitExpr(node.getExpression(), arg));
    }

    @Override
    public CNode visit(TryStatement node, CNode arg) {
        TryHelper helper = new TryHelper(this);
        if (node.getFinally() == null) {
            return helper.no_finally(node);
        }
        else {
            return helper.with_finally(node);
        }
    }

    @Override
    public CNode visit(IfStatement node, CNode arg) {
        CIfStatement ifStatement = new CIfStatement();
        ifStatement.condition = (CExpression) visitExpr(node.getExpression(), arg);
        ifStatement.thenStatement = (CStatement) visitExpr(node.getThenStatement(), arg);
        if (node.getElseStatement() != null) {
            ifStatement.elseStatement = (CStatement) visitExpr(node.getElseStatement(), arg);
        }
        return ifStatement;
    }


    @Override
    public CNode visit(ForStatement node, CNode arg) {
        CForStatement forStatement = new CForStatement();
        for (Expression init : (List<Expression>) node.initializers()) {
            forStatement.initializers.add((CExpression) visitExpr(init, arg));
        }
        forStatement.condition = (CExpression) visitExpr(node.getExpression(), arg);
        for (Expression updater : (List<Expression>) node.updaters()) {
            forStatement.updaters.add((CExpression) visitExpr(updater, arg));
        }
        forStatement.body = (CStatement) visitExpr(node.getBody(), arg);
        return forStatement;
    }

    @Override
    public CNode visit(EnhancedForStatement node, CNode arg) {
        CForEachStatement forEachStatement = new CForEachStatement();
        forEachStatement.body = (CStatement) visitExpr(node.getBody(), arg);
        forEachStatement.left = (CSingleVariableDeclaration) visit(node.getParameter(), arg);
        forEachStatement.right = (CExpression) visitExpr(node.getExpression(), arg);
        if (Config.use_vector) {
            forEachStatement.right = new DeferenceExpr(forEachStatement.right);
        }
        else {
            //todo java.iterator() or c++ type iterator
        }
        return forEachStatement;
    }

    @Override
    public CNode visit(SingleVariableDeclaration node, CNode arg) {
        CSingleVariableDeclaration variableDeclaration = new CSingleVariableDeclaration();
        variableDeclaration.type = typeVisitor.visitType(node.getType(), clazz);
        variableDeclaration.name = new CName(node.getName().getIdentifier());
        return variableDeclaration;
    }

    @Override
    public CNode visit(WhileStatement node, CNode arg) {
        CWhileStatement whileStatement = new CWhileStatement();
        whileStatement.expression = (CExpression) visitExpr(node.getExpression(), arg);
        whileStatement.setStatement((CStatement) visitExpr(node.getBody(), arg));
        return whileStatement;
    }

    @Override
    public CNode visit(DoStatement node, CNode arg) {
        CDoStatement doStatement = new CDoStatement();
        doStatement.body = (CStatement) visitExpr(node.getBody(), arg);
        doStatement.expression = (CExpression) visitExpr(node.getExpression(), arg);
        return doStatement;
    }

    @Override
    public CNode visit(YieldStatement node, CNode arg) {
        throw new RuntimeException("yield");
    }

    @Override
    public CNode visit(SwitchStatement node, CNode arg) {
        Expression expression = node.getExpression();
        ITypeBinding binding = expression.resolveTypeBinding();
        if (binding.isPrimitive()) {
            return new SwitchHelper(this).makeIfElse(node);
        }
        else if (binding.isEnum()) {
            //if else with ordinals
            SwitchHelper helper = new SwitchHelper(this);
            helper.isEnum = true;
            return helper.makeIfElse(node);
        }
        else {
            //multiple if elses
            SwitchHelper helper = new SwitchHelper(this);
            return helper.makeIfElse(node);
        }
    }

    @Override
    public CNode visit(LabeledStatement node, CNode arg) {
        return new CLabeledStatement(node.getLabel().getIdentifier());
    }

    @Override
    public CNode visit(ExpressionStatement node, CNode arg) {
        return new CExpressionStatement((CExpression) visitExpr(node.getExpression(), arg));
    }

    @Override
    public CNode visit(ConstructorInvocation node, CNode arg) {
        Call thisCall = new Call();
        for (Expression ar : (List<Expression>) node.arguments()) {
            thisCall.args.add((CExpression) visitExpr(ar, arg));
        }
        thisCall.isThis = true;
        thisCall.type = clazz.getType();
        method.thisCall = thisCall;
        return new CEmptyStatement();
    }

    @Override
    public CNode visit(SuperConstructorInvocation node, CNode arg) {
        //todo not so simple
        Call superCall = new Call();
        superCall.isThis = false;
        for (Expression ar : (List<Expression>) node.arguments()) {
            superCall.args.add((CExpression) visitExpr(ar, arg));
        }
        superCall.type = clazz.base.get(0);
        method.superCall = superCall;
        return new CEmptyStatement();
    }

    @Override
    public CNode visit(SynchronizedStatement node, CNode arg) {
        CBlockStatement block = (CBlockStatement) visit(node.getBody(), arg);
        block.addStatement(0, new CLineCommentStatement("synchronized"));
        return block;
    }

    @Override
    public CNode visit(VariableDeclarationStatement node, CNode arg) {
        //split single or keep multi?
        CVariableDeclarationStatement variableDeclaration = new CVariableDeclarationStatement();
        CType type = typeVisitor.visitType(node.getType(), clazz);
        variableDeclaration.setType(type);
        for (VariableDeclarationFragment frag : (List<VariableDeclarationFragment>) node.fragments()) {
            CVariableDeclarationFragment fragment = new CVariableDeclarationFragment();
            fragment.name = new CName(frag.getName().getIdentifier());
            fragment.initializer = (CExpression) visitExpr(frag.getInitializer(), arg);

            variableDeclaration.fragments.add(fragment);
        }
        return variableDeclaration;
    }

    @Override
    public CNode visit(VariableDeclaration node, CNode arg) {
        return null;
    }

    @Override
    public CNode visit(FieldAccess node, CNode arg) {
        IVariableBinding variableBinding = node.resolveFieldBinding();
        if (variableBinding != null && Config.writeLibHeader && variableBinding.isField()) {
            LibImplHandler.instance.addField(variableBinding);
        }


        CFieldAccess fieldAccess = new CFieldAccess();
        Expression scope = node.getExpression();
        CExpression scopeExpr = (CExpression) visitExpr(scope, arg);
        fieldAccess.scope = scopeExpr;

        ITypeBinding typeBinding = scope.resolveTypeBinding();
        if (typeBinding == null) {
            Logger.logBinding(clazz, node.toString());
        }
        else {
            if (typeBinding.isArray() && node.getName().getIdentifier().equals("length")) {
                if (Config.use_vector) {
                    CMethodInvocation invocation = new CMethodInvocation();
                    invocation.name = new CName("size");
                    invocation.scope = scopeExpr;
                    invocation.isArrow = true;
                    return invocation;
                }
            }
        }

        if (scope instanceof Name) {//field/var or class
            String scopeName = ((Name) scope).getFullyQualifiedName();
            if (scopeName.equals("this")) {
                fieldAccess.isArrow = true;
                fieldAccess.name = (CName) visit(node.getName(), arg);
                return fieldAccess;
            }
            IVariableBinding binding = node.resolveFieldBinding();

            if (binding.isEnumConstant()) {
                fieldAccess.isArrow = false;
            }
            else {
                fieldAccess.isArrow = !Modifier.isStatic(binding.getModifiers());
            }

        }
        else {//another expr
            fieldAccess.isArrow = true;
        }
        fieldAccess.name = new CName(node.getName().getIdentifier());
        return fieldAccess;
    }

    @Override
    public CNode visit(ClassInstanceCreation node, CNode arg) {
        ITypeBinding binding = node.getType().resolveBinding();
        CClassInstanceCreation creation;
        if (node.getAnonymousClassDeclaration() == null) {
            creation = new CClassInstanceCreation();
            creation.setType(typeVisitor.visitType(node.getType(), clazz));
            for (Expression expression : (List<Expression>) node.arguments()) {
                creation.args.add((CExpression) visitExpr(expression, arg));
            }
        }
        else {
            creation = AnonyHandler.handle(node.getAnonymousClassDeclaration(), typeVisitor.visitType(node.getType(), clazz), clazz, this);
        }
        if (Config.outer_ref_cons_arg) {
            //append arg
            creation.args.add(new CThisExpression());
        }
        else if (binding != null && !Modifier.isStatic(binding.getModifiers()) && (binding.isAnonymous() || binding.isNested())) {
            //append setRef
            CMethodInvocation invocation = new CMethodInvocation();
            invocation.isArrow = true;
            invocation.name = CName.from(Config.refSetterName);
            invocation.arguments.add(new CThisExpression());
            invocation.scope = creation;
            return invocation;
        }
        return creation;
    }

    @Override
    public CNode visit(ConditionalExpression node, CNode arg) {
        CTernaryExpression ternaryExpression = new CTernaryExpression();
        ternaryExpression.condition = (CExpression) visitExpr(node.getExpression(), arg);
        ternaryExpression.thenExpr = (CExpression) visitExpr(node.getThenExpression(), arg);
        ternaryExpression.elseExpr = (CExpression) visitExpr(node.getElseExpression(), arg);
        return ternaryExpression;
    }

    @Override
    public CNode visit(SuperMethodInvocation node, CNode arg) {
        if (node.getQualifier() != null) {
            throw new RuntimeException("qualified SuperMethodInvocation");
        }
        CMethodInvocation methodInvocation = new CMethodInvocation();
        methodInvocation.scope = clazz.getSuper();
        methodInvocation.name = (CName) visitName(node.getName(), null);
        methodInvocation.isArrow = false;
        args(node.arguments(), methodInvocation);
        return methodInvocation;
    }

    @Override
    public CNode visit(ThisExpression node, CNode arg) {
        return new CThisExpression();
    }

    @Override
    public CNode visit(VariableDeclarationExpression node, CNode arg) {
        CVariableDeclarationExpression variableDeclaration = new CVariableDeclarationExpression();
        variableDeclaration.type = typeVisitor.visitType(node.getType(), clazz);
        for (VariableDeclarationFragment frag : (List<VariableDeclarationFragment>) node.fragments()) {
            CVariableDeclarationFragment declaration = new CVariableDeclarationFragment();
            declaration.name = (CName) visit(frag.getName(), arg);
            declaration.initializer = (CExpression) visitExpr(frag.getInitializer(), arg);
            variableDeclaration.fragments.add(declaration);
        }
        return variableDeclaration;
    }

    @Override
    public CNode visit(NumberLiteral node, CNode arg) {
        return new CNumberLiteral(node.getToken());
    }

    @Override
    public CNode visit(SuperFieldAccess node, CNode arg) {
        throw new RuntimeException("SuperFieldAccess");
    }

    @Override
    public CNode visit(InfixExpression node, CNode arg) {
        //todo if string concatenation make string type non pointer so that '+' operator work
        CInfixExpression infixExpression = new CInfixExpression();
        infixExpression.operator = node.getOperator().toString();
        infixExpression.left = (CExpression) visitExpr(node.getLeftOperand(), arg);
        infixExpression.right = (CExpression) visitExpr(node.getRightOperand(), arg);
        if (node.hasExtendedOperands()) {
            for (Expression expression : (List<Expression>) node.extendedOperands()) {
                CExpression exp = (CExpression) visitExpr(expression, arg);
                infixExpression.right = new CInfixExpression(infixExpression.right, exp, infixExpression.operator);
            }
        }
        return infixExpression;
    }

    @Override
    public CNode visit(PostfixExpression node, CNode arg) {
        CPostfixExpression postfixExpression = new CPostfixExpression();
        postfixExpression.operator = node.getOperator().toString();
        postfixExpression.expression = (CExpression) visitExpr(node.getOperand(), arg);
        return postfixExpression;
    }

    @Override
    public CNode visit(PrefixExpression node, CNode arg) {
        CPrefixExpression prefixExpression = new CPrefixExpression();
        prefixExpression.operator = node.getOperator().toString();
        prefixExpression.expression = (CExpression) visitExpr(node.getOperand(), arg);
        return prefixExpression;
    }

    @Override
    public CNode visit(InstanceofExpression node, CNode arg) {
        source.hasRuntime = true;
        CMethodInvocation invocation = new CMethodInvocation();
        invocation.arguments.add((CExpression) visitExpr(node.getLeftOperand(), arg));
        invocation.name = new CName("instance_of");
        CType type = typeVisitor.visitType(node.getRightOperand(), clazz);
        invocation.name.typeArgs.add(type);
        return invocation;
    }

    @Override
    public CNode visit(ArrayAccess node, CNode arg) {
        CExpression expr = (CExpression) visitExpr(node.getArray(), arg);
        CExpression index = (CExpression) visitExpr(node.getIndex(), arg);

        if (Config.use_vector) {
            if (Config.array_access_bracket) {
                CArrayAccess arrayAccess = new CArrayAccess();
                arrayAccess.left = expr;
                arrayAccess.index = index;
                return arrayAccess;
            }
            else {
                CMethodInvocation invocation = new CMethodInvocation();
                invocation.name = new CName("at");
                invocation.isArrow = true;
                invocation.scope = expr;
                invocation.arguments.add(index);
                return invocation;
            }
        }
        else {
            //todo custom array
            CArrayAccess arrayAccess = new CArrayAccess();
            arrayAccess.left = expr;
            arrayAccess.index = index;
            return arrayAccess;
        }
    }

    @Override
    public CNode visit(ArrayCreation node, CNode arg) {
        if (node.getInitializer() != null) {
            CType type = typeVisitor.visitType(node.getType(), clazz).copy();
            //initializer doesn't need dimensions
            //todo nope
            CClassInstanceCreation creation = new CClassInstanceCreation();
            creation.setType(type);
            type.setPointer(false);
            creation.args.add((CExpression) visitExpr(node.getInitializer(), arg));
            return creation;
        }
        else {
            List<CExpression> list = list(node.dimensions());
            //set missing dims to zero
            for (int i = node.dimensions().size(); i < node.getType().getDimensions(); i++) {
                list.add(new CNumberLiteral("0"));
            }
            CType elemType = typeVisitor.visitType(node.getType().getElementType(), clazz);
            return ArrayHelper.makeRight(elemType, list);
        }
    }

    //{...}
    @Override
    public CNode visit(ArrayInitializer node, CNode arg) {
        return new CArrayInitializer(list(node.expressions()));
    }

    @Override
    public CNode visit(CastExpression node, CNode arg) {
        CCastExpression castExpression = new CCastExpression();
        castExpression.expression = (CExpression) visitExpr(node.getExpression(), arg);
        castExpression.setTargetType(typeVisitor.visitType(node.getType(), clazz));
        return castExpression;
    }

    @Override
    public CNode visit(ParenthesizedExpression node, CNode arg) {
        return new CParenthesizedExpression((CExpression) visitExpr(node.getExpression(), arg));
    }

    @SuppressWarnings("unchecked")
    @Override
    public CNode visit(MethodInvocation node, CNode arg) {
        CMethodInvocation invocation = new CMethodInvocation();
        invocation.name = (CName) visit(node.getName(), null);
        args(node.arguments(), invocation);
        Expression scope = node.getExpression();
        CExpression scopeExpr = scope == null ? null : (CExpression) visitExpr(scope, arg);
        IMethodBinding binding = node.resolveMethodBinding();

        if (binding == null) {
            Logger.log(clazz, node.toString() + " has null binding ,conversion may have problems");
        }

        boolean isStatic = binding != null && Modifier.isStatic(binding.getModifiers());
        boolean isAbstract = binding != null && Modifier.isAbstract(binding.getModifiers());
        //scoped static
        //non scoped static
        //outer
        invocation.scope = scopeExpr;
        if (scope != null) {
            invocation.isArrow = !isStatic;
        }

        if (binding != null && !binding.getDeclaringClass().isFromSource() && Config.writeLibHeader) {
            //Logger.log(clazz, node.getName().getIdentifier());
            LibImplHandler.instance.addMethod(binding);
        }

        if (scope == null && binding != null) {
            CType type = typeVisitor.fromBinding(binding.getDeclaringClass());
            //static outer method
            if (Modifier.isStatic(binding.getModifiers())) {
                invocation.scope = type;
                invocation.isArrow = false;
                return invocation;
            }
            //parent/super method
            if (isSuper(BindingMap.get(clazz.getType()), BindingMap.get(type))) {
                //qualify super method,not needed but more precise
                invocation.scope = clazz.getSuper();
                invocation.isArrow = false;
                return invocation;
            }
            //outer method
            if (!clazz.isStatic && !isAbstract && !type.equals(clazz.getType()) && !isSame(this.binding, binding.getDeclaringClass())) {
                //parent method called, set parent as scope
                invocation.isArrow = true;
                CExpression ref = ref(clazz, type);
                if (ref == null) {
                    return invocation;
                }
                invocation.scope = ref;
                return invocation;
            }
        }
        return invocation;
    }

    boolean isSame(ITypeBinding from, ITypeBinding to) {
        if (!from.getBinaryName().isEmpty()) {
            return from.getBinaryName().equals(to.getBinaryName());
        }
        //todo
        return from.isSubTypeCompatible(to);
    }

    //is base is super of type
    boolean isSuper(ITypeBinding from, ITypeBinding to) {
        if (from == null || from.getSuperclass() == null) {
            return false;
        }
        if (from.getSuperclass().equals(to)) {
            return true;
        }
        for (ITypeBinding binding : from.getInterfaces()) {
            if (binding.equals(to)) {
                //todo more depth
                return true;
            }
        }
        if (isSuper(from.getSuperclass(), to)) {
            return true;
        }
        return false;
    }

    private void args(List<Expression> arguments, CMethodInvocation methodInvocation) {
        methodInvocation.arguments.addAll(list(arguments));
    }

    private List<CExpression> list(List<Expression> arguments) {
        List<CExpression> list = new ArrayList<>();
        for (Expression expression : arguments) {
            list.add((CExpression) visitExpr(expression, null));
        }
        return list;
    }

    CNode resolvedName(SimpleName node) {
        IBinding binding = node.resolveBinding();
        CName name = new CName(node.getIdentifier());
        if (binding != null) {
            boolean isStatic = Modifier.isStatic(binding.getModifiers());
            if (binding.getKind() == IBinding.VARIABLE) {
                IVariableBinding variableBinding = (IVariableBinding) binding;
                if (variableBinding.isField()) {
                    CType type = typeVisitor.fromBinding(variableBinding.getDeclaringClass());
                    //static field,qualify
                    if (isStatic) {
                        CFieldAccess fieldAccess = new CFieldAccess();
                        fieldAccess.name = name;
                        fieldAccess.isArrow = false;
                        fieldAccess.scope = type;
                        return fieldAccess;
                    }
                    if (isSuper(this.binding, variableBinding.getDeclaringClass())) {
                        return name;
                    }
                    if (!type.equals(clazz.getType()) && !isSame(this.binding, variableBinding.getDeclaringClass())) {
                        //check if we are non static inner or anonymous class
                        CExpression rr = ref(clazz, type);
                        if (rr == null) {
                            Logger.log(clazz, "can't find name ref " + name);
                            return name;
                        }
                        CFieldAccess fieldAccess = new CFieldAccess();
                        fieldAccess.name = name;
                        fieldAccess.isArrow = true;
                        fieldAccess.scope = rr;
                        return fieldAccess;
                    }
                }
            }
        }
        return name;
    }

    CExpression ref(CClass from, CType target) {
        if (from.parent == null) {
            return null;
        }
        if (from.parent.getType().equals(target)) {
            return new CName(Config.parentName);
        }
        CExpression ref = ref(from.parent, target);
        if (ref == null) {
            return null;
        }
        CFieldAccess fieldAccess = new CFieldAccess();
        fieldAccess.scope = ref;
        fieldAccess.name = new CName(Config.parentName);
        fieldAccess.isArrow = true;
        return fieldAccess;
    }

    @Override
    public CNode visit(SimpleName node, CNode arg) {
        return resolvedName(node);
    }

    //qualified class,array.length,field access
    @Override
    public CNode visit(QualifiedName node, CNode arg) {
        IBinding binding = node.resolveBinding();

        if ((binding instanceof IVariableBinding) && Config.writeLibHeader) {
            LibImplHandler.instance.addField((IVariableBinding) binding);
        }

        if (binding == null) {
            Logger.logBinding(clazz, node.toString());
            //normal qualified name access
            return new CName(node.getFullyQualifiedName());
        }
        else {
            ITypeBinding typeBinding = node.getQualifier().resolveTypeBinding();

            if (typeBinding == null) {
                //qualifier is not a type so it is a package
                return new CName(node.getFullyQualifiedName());
            }

            boolean isStatic = Modifier.isStatic(binding.getModifiers());
            CType type = typeVisitor.fromBinding(typeBinding);
            type.isPointer = false;
            CExpression scope = (CExpression) visitExpr(node.getQualifier(), arg);


            if (typeBinding.isArray() && node.getName().getIdentifier().equals("length")) {
                IVariableBinding variableBinding = (IVariableBinding) binding;
                if (variableBinding.getDeclaringClass() == null) {//array.length
                    if (Config.use_vector) {
                        CMethodInvocation methodInvocation = new CMethodInvocation();
                        methodInvocation.isArrow = true;
                        methodInvocation.scope = scope;
                        methodInvocation.name = new CName("size");
                        return methodInvocation;
                    }
                }
            }


            if (isStatic) {
                CFieldAccess fieldAccess = new CFieldAccess();
                fieldAccess.name = new CName(node.getName().getIdentifier());
                fieldAccess.isArrow = false;
                fieldAccess.scope = type;
                return fieldAccess;
            }
            else {
                CFieldAccess fieldAccess = new CFieldAccess();
                fieldAccess.name = new CName(node.getName().getIdentifier());
                fieldAccess.isArrow = true;
                fieldAccess.scope = scope;

                return fieldAccess;
            }
        }
    }
}
