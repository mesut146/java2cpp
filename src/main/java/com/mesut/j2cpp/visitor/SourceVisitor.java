package com.mesut.j2cpp.visitor;

import com.mesut.j2cpp.Converter;
import com.mesut.j2cpp.ast.*;
import com.mesut.j2cpp.cppast.CClassImpl;
import com.mesut.j2cpp.cppast.CExpression;
import com.mesut.j2cpp.cppast.CNode;
import com.mesut.j2cpp.cppast.CStatement;
import com.mesut.j2cpp.cppast.expr.*;
import com.mesut.j2cpp.cppast.literal.*;
import com.mesut.j2cpp.cppast.stmt.*;
import com.mesut.j2cpp.util.Helper;
import org.eclipse.jdt.core.dom.*;

import java.util.List;

@SuppressWarnings("unchecked")
public class SourceVisitor extends DefaultVisitor<CNode, CNode> {

    Converter converter;
    CSource source;
    TypeVisitor typeVisitor;
    CClass clazz;
    CMethod method;

    public SourceVisitor(Converter converter, CSource source) {
        this.converter = converter;
        this.source = source;
        typeVisitor = new TypeVisitor(source.header);
    }

    public CHeader getHeader() {
        return source.header;
    }

    public void convert() {
        //System.out.printf("count %s = %s\n", source.header.rpath, source.header.classes.size());
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
                res.statements.add(cStatement);
            }
        }
        return res;
    }

    @Override
    public CNode visit(TypeDeclarationStatement node, CNode arg) {
        TypeDeclaration typeDeclaration = (TypeDeclaration) node.getDeclaration();
        DeclarationVisitor visitor = new DeclarationVisitor(this, typeVisitor);
        CClass cls = visitor.visit(typeDeclaration, null);
        return cls;
    }

    @Override
    public CNode visit(AssertStatement node, CNode arg) {
        return new CEmptyStatement();
    }


    @Override
    public CNode visit(StringLiteral node, CNode arg) {
        CObjectCreation objectCreation = new CObjectCreation();
        objectCreation.type = Helper.getStringType();
        objectCreation.type.setHeader(source.header);
        objectCreation.args.add(new CStringLiteral(node.getLiteralValue()));
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
        //Class.forName(type)
        CType type = typeVisitor.visitType(node.getType());
        CMethodInvocation methodInvocation = new CMethodInvocation();
        CName cls = new CName("Class");
        cls.namespace = new Namespace("java.lang");
        methodInvocation.scope = cls;
        methodInvocation.name = new CName("forName");
        methodInvocation.isArrow = true;
        methodInvocation.arguments.add(new CStringLiteral(type.toString()));
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
        return new CReturnStatement((CExpression) visitExpr(node.getExpression(), arg));
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
        return forEachStatement;
    }

    @Override
    public CNode visit(SingleVariableDeclaration node, CNode arg) {
        CSingleVariableDeclaration variableDeclaration = new CSingleVariableDeclaration();
        variableDeclaration.type = typeVisitor.visitType(node.getType());
        variableDeclaration.name = new CName(node.getName().getIdentifier());
        return variableDeclaration;
    }

    @Override
    public CNode visit(WhileStatement node, CNode arg) {
        CWhileStatement whileStatement = new CWhileStatement();
        whileStatement.expression = (CExpression) visitExpr(node.getExpression(), arg);
        whileStatement.statement = (CStatement) visitExpr(node.getBody(), arg);
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
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public CNode visit(MethodInvocation node, CNode arg) {
        CMethodInvocation methodInvocation = new CMethodInvocation();
        if (node.getExpression() != null) {//scope
            Expression scope = node.getExpression();
            methodInvocation.scope = (CExpression) visitExpr(scope, arg);
            IMethodBinding binding = node.resolveMethodBinding();
            ITypeBinding typeBinding = binding.getDeclaringClass();


            if (scope instanceof Name) {//field or class or variable
                if (Modifier.isStatic(binding.getModifiers())) {
                    methodInvocation.isArrow = false;
                    methodInvocation.scope = new CType(typeBinding.getQualifiedName(), source.header);
                }
                String scopeName = ((Name) scope).getFullyQualifiedName();
                if (scopeName.equals("this")) {
                    methodInvocation.isArrow = true;
                    methodInvocation.name = (CName) visit(node.getName(), null);
                    args((List<Expression>) node.arguments(), methodInvocation);
                    return methodInvocation;
                }
                methodInvocation.isArrow = !Modifier.isStatic(binding.getModifiers());
            }
            else {
                //another method call or field access
                methodInvocation.isArrow = true;
            }
        }
        methodInvocation.name = new CName(node.getName().getIdentifier());
        args(node.arguments(), methodInvocation);
        return methodInvocation;
    }

    private void args(List<Expression> arguments, CMethodInvocation methodInvocation) {
        for (Expression expression : arguments) {
            methodInvocation.arguments.add((CExpression) visitExpr(expression, null));
        }
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
            //var->ordinal
            /*CMethodInvocation methodInvocation = new CMethodInvocation();
            methodInvocation.isArrow = true;
            methodInvocation.scope = (CExpression) visit(expression, null);
            methodInvocation.name = new CName("ordinal");*/

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
    public CNode visit(SwitchCase node, CNode arg) {
        return null;
    }

    @Override
    public CNode visit(SynchronizedStatement node, CNode arg) {
        return null;
    }

    @Override
    public CNode visit(ThrowStatement node, CNode arg) {
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
    public CNode visit(VariableDeclarationStatement node, CNode arg) {
        //split single or keep multi?
        CVariableDeclarationStatement variableDeclaration = new CVariableDeclarationStatement();
        CType type = typeVisitor.visitType(node.getType());
        variableDeclaration.type = type;
        for (VariableDeclarationFragment frag : (List<VariableDeclarationFragment>) node.fragments()) {
            /*CSingleVariableDeclaration declaration = new CSingleVariableDeclaration();
            declaration.type = type;
            declaration.name = (CName) visit(frag.getName(), null);

            if (frag.getInitializer() != null) {
                declaration.expression = (CExpression) visit(frag.getInitializer(), arg);

            }*/
            //----
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
        CFieldAccess fieldAccess = new CFieldAccess();
        Expression scope = node.getExpression();
        fieldAccess.scope = (CExpression) visitExpr(scope, arg);

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
        if (node.getAnonymousClassDeclaration() == null) {
            CClassInstanceCreation classInstanceCreation = new CClassInstanceCreation();
            classInstanceCreation.type = typeVisitor.visitType(node.getType());
            classInstanceCreation.type.isPointer = false;
            for (Expression expression : (List<Expression>) node.arguments()) {
                classInstanceCreation.args.add((CExpression) visitExpr(expression, arg));
            }
            return classInstanceCreation;
        }
        else {
            //System.out.printf("anony = %s %s\n", clazz.name, method != null ? method.getName() + "()" : "field");
            CClass anony = new CClass();
            anony.isAnonymouse = true;
            anony.name = clazz.getAnonyName();
            anony.ns = clazz.ns;
            anony.base.add(typeVisitor.visitType(node.getType()));
            AnonymousClassDeclaration declaration = node.getAnonymousClassDeclaration();
            DeclarationVisitor declarationVisitor = new DeclarationVisitor(this, typeVisitor);
            //todo find local & class references
            for (BodyDeclaration body : (List<BodyDeclaration>) declaration.bodyDeclarations()) {
                if (body instanceof FieldDeclaration) {
                    declarationVisitor.visit((FieldDeclaration) body, anony);
                    //throw new RuntimeException("anonymous field");
                }
                else if (body instanceof MethodDeclaration) {
                    CMethod method = (CMethod) declarationVisitor.visit((MethodDeclaration) body, anony);
                    anony.addMethod(method);
                }
                else {
                    throw new RuntimeException("ClassInstanceCreation anony");
                }
            }
            CClassInstanceCreation classInstanceCreation = new CClassInstanceCreation();
            classInstanceCreation.type = new CType(anony.name, source.header);
            source.anony.add(new CClassImpl(anony));
            return classInstanceCreation;
        }
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
        throw new RuntimeException("SuperMethodInvocation");
    }

    @Override
    public CNode visit(ThisExpression node, CNode arg) {
        return new CThisExpression();
    }

    @Override
    public CNode visit(VariableDeclarationExpression node, CNode arg) {
        CVariableDeclarationExpression variableDeclaration = new CVariableDeclarationExpression();
        variableDeclaration.type = typeVisitor.visitType(node.getType());
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
        CNumberLiteral numberLiteral = new CNumberLiteral();
        numberLiteral.value = node.getToken();
        return numberLiteral;
    }

    @Override
    public CNode visit(SuperFieldAccess node, CNode arg) {
        throw new RuntimeException("SuperFieldAccess");
    }

    @Override
    public CNode visit(InfixExpression node, CNode arg) {
        CInfixExpression infixExpression = new CInfixExpression();
        infixExpression.operator = node.getOperator().toString();
        infixExpression.left = (CExpression) visitExpr(node.getLeftOperand(), arg);
        infixExpression.right = (CExpression) visitExpr(node.getRightOperand(), arg);
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
        CType type = typeVisitor.visitType(node.getRightOperand());
        invocation.name.typeArgs.add(type);
        return invocation;
    }

    @Override
    public CNode visit(ArrayAccess node, CNode arg) {
        CArrayAccess arrayAccess = new CArrayAccess();
        arrayAccess.left = (CExpression) visitExpr(node.getArray(), arg);
        arrayAccess.index = (CExpression) visitExpr(node.getIndex(), arg);
        return arrayAccess;
    }

    @Override
    public CNode visit(ArrayCreation node, CNode arg) {
        if (node.getInitializer() != null) {
            return visit(node.getInitializer(), arg);
        }
        else {
            CType typeName = typeVisitor.visitType(node.getType()).copy();
            return new CArrayCreation2(new CArrayType(typeName, node.dimensions().size()));
        }
    }

    @Override
    public CNode visit(CastExpression node, CNode arg) {
        CCastExpression castExpression = new CCastExpression();
        castExpression.expression = (CExpression) visitExpr(node.getExpression(), arg);
        castExpression.targetType = typeVisitor.visitType(node.getType());
        return castExpression;
    }

    @Override
    public CNode visit(ParenthesizedExpression node, CNode arg) {
        return new CParenthesizedExpression((CExpression) visitExpr(node.getExpression(), arg));
    }

    //{...}
    @Override
    public CNode visit(ArrayInitializer node, CNode arg) {
        CArrayInitializer initializer = new CArrayInitializer();
        for (Expression expression : (List<Expression>) node.expressions()) {
            initializer.expressions.add((CExpression) visitExpr(expression, arg));
        }
        return initializer;
    }

    CNode resolvedName(SimpleName node) {
        IBinding binding = node.resolveBinding();
        if (binding != null && Modifier.isStatic(binding.getModifiers())) {
            if (binding.getKind() == IBinding.VARIABLE) {
                IVariableBinding variableBinding = (IVariableBinding) binding;
                String qu = variableBinding.getDeclaringClass().getQualifiedName();

                CFieldAccess fieldAccess = new CFieldAccess();
                fieldAccess.name = new CName(node.getIdentifier());
                fieldAccess.isArrow = false;
                CType scope = new CType(qu.replace(".", "::"), getHeader());
                fieldAccess.scope = source.normalizeType(scope);
                return fieldAccess;
            }
            else if (binding.getKind() == IBinding.METHOD) {
                IMethodBinding methodBinding = (IMethodBinding) binding;
            }
        }

        ITypeBinding typeBinding = node.resolveTypeBinding();
        if (typeBinding != null) {

            /*CType type = typeVisitor.fromBinding(binding);
            return type;*/
        }
        return new CName(node.getIdentifier());
    }


    @Override
    public CNode visit(SimpleName node, CNode arg) {
        return resolvedName(node);
        //return new CName(node.getIdentifier());
    }

    @Override
    public CNode visit(QualifiedName node, CNode arg) {
        IBinding binding = node.resolveBinding();
        boolean isStatic = Modifier.isStatic(binding.getModifiers());
        if (isStatic || binding instanceof IVariableBinding) {
            CFieldAccess fieldAccess = new CFieldAccess();
            fieldAccess.name = new CName(node.getName().getIdentifier());
            fieldAccess.isArrow = !isStatic;
            fieldAccess.scope = (CExpression) visitExpr(node.getQualifier(), arg);
            return fieldAccess;
        }
        return new CName(node.getFullyQualifiedName());
    }
}
