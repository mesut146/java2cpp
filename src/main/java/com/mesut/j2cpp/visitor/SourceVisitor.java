package com.mesut.j2cpp.visitor;

import com.mesut.j2cpp.Config;
import com.mesut.j2cpp.LibHandler;
import com.mesut.j2cpp.Logger;
import com.mesut.j2cpp.ast.*;
import com.mesut.j2cpp.cppast.*;
import com.mesut.j2cpp.cppast.expr.*;
import com.mesut.j2cpp.cppast.literal.*;
import com.mesut.j2cpp.cppast.stmt.*;
import com.mesut.j2cpp.map.Mapper;
import com.mesut.j2cpp.util.ArrayHelper;
import com.mesut.j2cpp.util.TypeHelper;
import org.eclipse.jdt.core.dom.*;

import java.util.*;

@SuppressWarnings("unchecked")
public class SourceVisitor extends DefaultVisitor<CNode, CNode> {

    CSource source;
    CClass clazz;
    CMethod method;
    ITypeBinding binding;
    CBlockStatement block;
    String catchName;

    public SourceVisitor(CSource source) {
        this.source = source;
    }

    //make str to std::string
    public static CExpression stringCreation(CStringLiteral node, CClass clazz) {
        CObjectCreation obj = new CObjectCreation();
        obj.type = Mapper.instance.mapType(TypeHelper.getStringType(), clazz);
        obj.args.add(node);
        return obj;
    }

    @Override
    public CNode visit(Block n, CNode arg) {
        CBlockStatement res = new CBlockStatement();
        block = res;
        for (Statement s : (List<Statement>) n.statements()) {
            CStatement statement = (CStatement) visitExpr(s, arg);
            if (!(statement instanceof CEmptyStatement)) {
                res.addStatement(statement);
            }
        }
        return res;
    }

    @Override
    public CNode visit(TypeDeclarationStatement node, CNode arg) {
        TypeDeclaration declaration = (TypeDeclaration) node.getDeclaration();
        DeclarationVisitor visitor = new DeclarationVisitor(this);
        return visitor.visit(declaration, null);
    }

    @Override
    public CNode visit(AssertStatement node, CNode arg) {
        return new CEmptyStatement();
    }

    @Override
    public CNode visit(StringLiteral node, CNode arg) {
        return stringCreation(new CStringLiteral(node.getLiteralValue(), node.getEscapedValue()), clazz);
    }

    @Override
    public CNode visit(BooleanLiteral node, CNode arg) {
        return new CBooleanLiteral(node.booleanValue());
    }

    @Override
    public CNode visit(CharacterLiteral node, CNode arg) {
        CCharacterLiteral literal = new CCharacterLiteral();
        literal.value = node.getEscapedValue();
        literal.charValue = node.charValue();
        return literal;
    }

    @Override
    public CNode visit(NullLiteral node, CNode arg) {
        return new CNullLiteral();
    }

    //type.class
    @Override
    public CNode visit(TypeLiteral node, CNode arg) {
        //Class::forName(type)
        CType type = TypeVisitor.visitType(node.getType(), clazz);
        CType classType = TypeHelper.getClassType();
        type.scope = source;
        classType.scope = source;

        CMethodInvocation methodInvocation = new CMethodInvocation();
        methodInvocation.scope = new CName(classType.basicForm());
        methodInvocation.name = new CName("forName");
        methodInvocation.isArrow = false;
        methodInvocation.arguments.add(new CStringLiteral(type.basicForm(), null));
        return methodInvocation;
    }


    @Override
    public CNode visit(EmptyStatement node, CNode arg) {
        return new CEmptyStatement();
    }

    @Override
    public CNode visit(Assignment node, CNode arg) {
        CAssignment assignment = new CAssignment();
        assignment.left = (CExpression) visitExpr(node.getLeftHandSide(), arg);
        assignment.operator = node.getOperator().toString();
        if (assignment.operator.equals(">>>=")) {
            assignment.operator = "=";
            assignment.right = makeUnsignedRshift(node.getLeftHandSide(), node.getRightHandSide());
            return assignment;
        }
        assignment.right = (CExpression) visitExpr(node.getRightHandSide(), arg);
        return assignment;
    }

    CInfixExpression makeUnsignedRshift(Expression left, Expression right) {
        CInfixExpression infix = new CInfixExpression();
        infix.left = new CName("(unsigned " + TypeVisitor.fromBinding(left.resolveTypeBinding()) + ")" + visitExpr(left, null));
        infix.right = (CExpression) visitExpr(right, null);
        infix.operator = ">>";
        return infix;
    }

    @Override
    public CNode visit(BreakStatement node, CNode arg) {
        if (node.getLabel() != null) {
            return new CBreakStatement(node.getLabel().getIdentifier());
        }
        return new CBreakStatement(null);
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
    public CNode visit(ThrowStatement node, CNode arg) {
        CExpression e = (CExpression) visitExpr(node.getExpression(), arg);
        //convert heap allocation into stack allocation
        if (e instanceof CClassInstanceCreation) {
            CClassInstanceCreation cc = (CClassInstanceCreation) e;
            CMethodInvocation res = new CMethodInvocation();
            res.name = new CName(cc.type.toString());
            res.arguments = cc.args;
            return new CThrowStatement(res);
        }
        //if throwing custom exception
        if (e instanceof CName && !((CName) e).is(catchName)) {
            return new CThrowStatement(new DeferenceExpr(e));
        }
        return new CThrowStatement(e);
    }

    @Override
    public CNode visit(TryStatement node, CNode arg) {
        TryHelper helper = new TryHelper(this, node);
        return helper.handle();
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
        forEachStatement.right = new DeferenceExpr(forEachStatement.right);
        return forEachStatement;
    }

    @Override
    public CNode visit(SingleVariableDeclaration node, CNode arg) {
        CSingleVariableDeclaration decl = new CSingleVariableDeclaration();
        if (Config.use_auto && !(node.getParent() instanceof CatchClause)) {
            decl.type = TypeHelper.getAutoType();
        }
        else {
            decl.type = TypeVisitor.visitType(node.getType(), clazz);
        }
        decl.name = new CName(node.getName().getIdentifier());
        return decl;
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
        SwitchHelper helper = new SwitchHelper(this);
        if (binding.isEnum()) {
            //if else with ordinals
            helper.isEnum = true;
        }
        else if (binding.getQualifiedName().equals("java.lang.String")) {
            helper.isString = true;
        }
        else if (binding.isPrimitive()) {
            return helper.makeNormal(node);
        }
        List<CStatement> list = helper.makeIfElse(node);
        return new CMultiStatement(list);
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
        Call superCall = new Call();
        superCall.isThis = false;
        superCall.args = list(node.arguments());
        superCall.type = clazz.superClass;
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
        CVariableDeclarationStatement decl = new CVariableDeclarationStatement();
        if (node.fragments().size() == 1 && Config.use_auto) {
            VariableDeclarationFragment f = (VariableDeclarationFragment) node.fragments().get(0);
            if (f.getInitializer() != null) {
                CVariableDeclarationFragment fragment = new CVariableDeclarationFragment();
                fragment.name = new CName(f.getName().getIdentifier());
                fragment.initializer = (CExpression) visitExpr(f.getInitializer(), arg);
                decl.fragments.add(fragment);
                if (node.getType().resolveBinding().equals(f.getInitializer().resolveTypeBinding())) {
                    decl.type = TypeHelper.getAutoType();
                }
                else {
                    decl.type = TypeVisitor.visitType(node.getType(), clazz);
                }
                return decl;
            }

        }
        decl.type = TypeVisitor.visitType(node.getType(), clazz);
        for (VariableDeclarationFragment frag : (List<VariableDeclarationFragment>) node.fragments()) {
            CVariableDeclarationFragment fragment = new CVariableDeclarationFragment();
            fragment.name = new CName(frag.getName().getIdentifier());
            fragment.initializer = (CExpression) visitExpr(frag.getInitializer(), arg);

            decl.fragments.add(fragment);
        }
        return decl;
    }

    @Override
    public CNode visit(VariableDeclaration node, CNode arg) {
        return null;
    }

    @Override
    public CNode visit(FieldAccess node, CNode arg) {
        IVariableBinding variableBinding = node.resolveFieldBinding();
        if (variableBinding != null && Config.writeLibHeader && variableBinding.isField()) {
            LibHandler.instance.addField(variableBinding);
        }

        CFieldAccess fieldAccess = new CFieldAccess();
        Expression scope = node.getExpression();
        CExpression scopeExpr = (CExpression) visitExpr(scope, arg);
        fieldAccess.scope = scopeExpr;

        ITypeBinding typeBinding = scope.resolveTypeBinding();
        if (typeBinding == null) {
            Logger.logBinding(clazz, node.toString());
        }
        else if (typeBinding.isArray() && node.getName().getIdentifier().equals("length")) {
            return new CMethodInvocation(scopeExpr, new CName("size"), true);
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
        //fieldAccess.name = new CName(node.getName().getIdentifier());
        Object o = visitExpr(node.getName(), arg);
        if (o instanceof CFieldAccess) {
            //todo Main.this.field
            fieldAccess.name = new CName(o.toString());
        }
        else {
            fieldAccess.name = (CName) o;
        }
        return fieldAccess;
    }

    @Override
    public CNode visit(ClassInstanceCreation node, CNode arg) {
        ITypeBinding binding = node.getType().resolveBinding();
        CClassInstanceCreation creation;
        CClass anony = null;
        if (node.getAnonymousClassDeclaration() == null) {
            creation = new CClassInstanceCreation();
            creation.setType(TypeVisitor.visitType(node.getType(), clazz));
        }
        else {
            AnonyHandler handler = new AnonyHandler();
            creation = handler.handle(node.getAnonymousClassDeclaration(), TypeVisitor.visitType(node.getType(), clazz), clazz, this);
            anony = handler.anony;
        }
        creation.args = list(node.arguments());
        if (binding == null) {
            Logger.logBinding(clazz, node.toString());
            return creation;
        }
        if (node.getAnonymousClassDeclaration() != null || !Modifier.isStatic(binding.getModifiers()) && binding.isNested()) {
            if (Config.outer_ref_cons_arg) {
                //append arg
                creation.args.add(new CThisExpression());
            }
            //append setRef
            CMethodInvocation invocation = new CMethodInvocation();
            invocation.isArrow = true;
            invocation.name = CName.from(Config.refSetterName);
            invocation.arguments.add(new CThisExpression());
            //set local var references
            if (node.getAnonymousClassDeclaration() != null) {
                for (CField field : anony.anonyFields) {
                    invocation.arguments.add(field.name);
                }
            }
            invocation.scope = new CParenthesizedExpression(creation);
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
        methodInvocation.arguments = list(node.arguments());
        return methodInvocation;
    }

    @Override
    public CNode visit(ThisExpression node, CNode arg) {
        if (node.getQualifier() == null) {
            return new CThisExpression();
        }
        ITypeBinding binding = node.getQualifier().resolveTypeBinding();
        return ref2(this.binding, binding);
    }

    @Override
    public CNode visit(VariableDeclarationExpression node, CNode arg) {
        CVariableDeclarationExpression variableDeclaration = new CVariableDeclarationExpression();
        variableDeclaration.type = TypeVisitor.visitType(node.getType(), clazz);
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
        if (node.getOperator().toString().equals("+")) {
            //if either side is string then whole expr is string concatenation
            if (isStr(node.getLeftOperand()) || isStr(node.getRightOperand())) {
                return infixString(node);
            }
        }
        if (node.getOperator().toString().equals(">>>")) {
            return makeUnsignedRshift(node.getLeftOperand(), node.getRightOperand());
        }

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

    boolean isStr(Expression e) {
        return e.resolveTypeBinding().getQualifiedName().equals("java.lang.String");
    }

    CExpression infixString(InfixExpression node) {
        CClassInstanceCreation creation = new CClassInstanceCreation();
        CInfixExpression res = new CInfixExpression();
        res.operator = "+";
        Expression left = node.getLeftOperand();
        Expression right = node.getRightOperand();
        res.left = makeStrIfNot(left);
        res.right = makeStrIfNot(right);
        if ((left instanceof StringLiteral || left instanceof CharacterLiteral) && (right instanceof StringLiteral || right instanceof CharacterLiteral)) {
            //one must be converted
            CMethodInvocation invocation = new CMethodInvocation();
            invocation.name = CName.simple("std::string");
            invocation.arguments.add(res.left);
            res.left = invocation;
        }
        if (node.hasExtendedOperands()) {
            for (Expression expression : (List<Expression>) node.extendedOperands()) {
                res.other.add(makeStrIfNot(expression));
            }
        }
        creation.args.add(res);
        creation.type = Mapper.instance.mapType(TypeHelper.getStringType(), clazz);
        return creation;
    }

    CExpression makeStrIfNot(Expression node) {
        if (node instanceof StringLiteral) {
            StringLiteral lit = (StringLiteral) node;
            return new CStringLiteral(lit.getLiteralValue(), lit.getEscapedValue());
        }
        else {
            ITypeBinding binding = node.resolveTypeBinding();
            CExpression expression = (CExpression) visitExpr(node, null);
            if (binding.isPrimitive()) {
                if (node instanceof CharacterLiteral) {
                    return new CStringLiteral("" + ((CharacterLiteral) node).charValue());
                }
                CMethodInvocation invocation = new CMethodInvocation();
                invocation.name = CName.simple("std::to_string");
                invocation.arguments.add(expression);
                return invocation;
            }
            else {
                if (binding.getQualifiedName().equals("java.lang.String")) {
                    return new DeferenceExpr(expression);
                }
                else {
                    //may have toString
                    for (IMethodBinding methodBinding : binding.getDeclaredMethods()) {
                        if (methodBinding.getName().equals("toString") && methodBinding.getReturnType().getQualifiedName().equals("java.lang.String")) {
                            CMethodInvocation invocation = new CMethodInvocation();
                            invocation.isArrow = true;
                            invocation.scope = expression;
                            invocation.name = new CName("toString");
                            return new DeferenceExpr(invocation);
                        }
                    }
                    //leave as it is
                    return expression;
                }
            }
        }
    }

    @Override
    public CNode visit(PostfixExpression node, CNode arg) {
        CPostfixExpression res = new CPostfixExpression();
        res.operator = node.getOperator().toString();
        res.expression = (CExpression) visitExpr(node.getOperand(), arg);
        return res;
    }

    @Override
    public CNode visit(PrefixExpression node, CNode arg) {
        CPrefixExpression res = new CPrefixExpression();
        res.operator = node.getOperator().toString();
        res.expression = (CExpression) visitExpr(node.getOperand(), arg);
        return res;
    }

    @Override
    public CNode visit(InstanceofExpression node, CNode arg) {
        source.hasRuntime = true;
        CMethodInvocation res = new CMethodInvocation();
        res.arguments.add((CExpression) visitExpr(node.getLeftOperand(), arg));
        res.name = new CName("instance_of");
        CType type = TypeVisitor.visitType(node.getRightOperand(), clazz);
        res.name.typeArgs.add(type);
        return res;
    }

    @Override
    public CNode visit(ArrayAccess node, CNode arg) {
        CExpression expr = (CExpression) visitExpr(node.getArray(), arg);
        CExpression index = (CExpression) visitExpr(node.getIndex(), arg);
        if (Config.array_access_bracket) {
            CArrayAccess arrayAccess = new CArrayAccess();
            arrayAccess.left = new DeferenceExpr(expr);
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

    @Override
    public CNode visit(ArrayCreation node, CNode arg) {
        if (node.getInitializer() != null) {
            CType type = TypeVisitor.visitType(node.getType(), clazz).copy();
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
            CType elemType = TypeVisitor.visitType(node.getType().getElementType(), clazz);
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
        CExpression expr = (CExpression) visitExpr(node.getExpression(), arg);
        CType type = TypeVisitor.visitType(node.getType(), clazz);
        type.isTypeArg = true;
        if (TypeHelper.canCast(node.getExpression(), node.getExpression().resolveTypeBinding(), node.getType().resolveBinding())) {
            //System.out.println("static "+clazz.getType());
            //static cast
            CCastExpression res = new CCastExpression();
            res.expression = expr;
            res.setTargetType(type);
            return res;
        }
        else {
            //System.out.println("dynamic " + clazz.getType());
            //dynamic e.g base class to base class
            CName nm = CName.from("dynamic_cast");
            nm.typeArgs.add(type);
            CMethodInvocation invocation = new CMethodInvocation(null, nm, false);
            invocation.arguments.add(expr);
            return invocation;
        }
    }

    @Override
    public CNode visit(ParenthesizedExpression node, CNode arg) {
        return new CParenthesizedExpression((CExpression) visitExpr(node.getExpression(), arg));
    }


    CExpression handlePrint(String name, Expression scope, List<Expression> args0, List<CExpression> args1) {
        String str = scope.toString();
        if (str.equals("System.out") || str.equals("java.lang.System.out") || str.equals("System.err") || str.equals("java.lang.System.err")) {
            boolean err = str.equals("System.err") || str.equals("java.lang.System.err");
            CName out = err ? CName.from("std::cerr") : CName.from("std::cout");
            //normalize arg
            CExpression arg = null;
            if (!args0.isEmpty()) {
                Expression arg0 = args0.get(0);
                if (arg0 instanceof StringLiteral) {
                    arg = new CStringLiteral(((StringLiteral) arg0).getLiteralValue());
                }
                else if (arg0 instanceof NumberLiteral) {
                    arg = new CNumberLiteral(arg0.toString());
                }
                else {
                    arg = new DeferenceExpr(new CParenthesizedExpression(args1.get(0)));
                }
            }
            if (name.equals("print") || name.equals("append")) {
                CInfixExpression infix = new CInfixExpression();
                infix.operator = "<<";
                infix.left = out;
                infix.right = arg;
                return infix;
            }
            else if (name.equals("println")) {
                CInfixExpression infix = new CInfixExpression();
                infix.operator = "<<";
                infix.left = out;
                infix.right = arg;
                if (arg == null) {
                    //just println()
                    infix.right = new CStringLiteral("\\n");
                    return infix;
                }
                return new CInfixExpression(infix, new CStringLiteral("\\n"), "<<");
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public CNode visit(MethodInvocation node, CNode arg0) {
        IMethodBinding binding = node.resolveMethodBinding();
        if (binding == null) {
            Logger.logBinding(clazz, node.toString());
            CMethodInvocation invocation = new CMethodInvocation();
            invocation.arguments = list(node.arguments());
            invocation.name = (CName) visit(node.getName(), null);
            if (node.getExpression() != null) invocation.scope = (CExpression) visitExpr(node.getExpression(), arg0);
            return invocation;
        }
        CExpression scope = node.getExpression() == null ? null : (CExpression) visitExpr(node.getExpression(), arg0);
        CMethodInvocation invocation = new CMethodInvocation();
        invocation.name = (CName) visit(node.getName(), null);
        invocation.arguments = list(node.arguments());

        boolean isStatic = Modifier.isStatic(binding.getModifiers());
        boolean isAbstract = Modifier.isAbstract(binding.getModifiers());
        //scoped static
        //non scoped static
        //outer
        invocation.scope = scope;

        if (!binding.getDeclaringClass().isFromSource() && Config.writeLibHeader) {
            LibHandler.instance.addMethod(binding);
        }

        if (scope != null) {
            CExpression p = handlePrint(binding.getName(), node.getExpression(), (List<Expression>) node.arguments(), invocation.arguments);
            if (p != null) return p;
            //mapper
            Mapper.Mapped target = Mapper.instance.mapMethod(binding, invocation.arguments, scope, clazz);
            if (target != null) {
                if (target.list == null) {
                    //single expr
                    return target.expr;
                }
                for (String line : target.list.split("\n")) {
                    block.addStatement(new RawStatement(line));
                }
                return target.expr;
            }
            invocation.isArrow = !isStatic;
            if (catchName != null && catchName.equals(scope.toString())) {
                invocation.isDot = true;
            }
        }

        ITypeBinding onType = binding.getDeclaringClass();
        CType type = TypeVisitor.fromBinding(onType, clazz);
        if (isStatic) {
            //static method needs qualifier
            invocation.scope = type;
            invocation.isArrow = false;
            return invocation;
        }

        if (scope != null) {
            //scope already resolved by resolvedName()
            return invocation;
        }

        //base method
        if (Config.qualifyBaseMethod && !this.binding.equals(onType) && this.binding.isSubTypeCompatible(onType) && !onType.isInterface()) {
            //qualify super method,not needed but more precise
            invocation.scope = TypeVisitor.fromBinding(onType);
            invocation.isArrow = false;
            return invocation;
        }
        invocation.isArrow = true;//assume pointer
        //check outer method
        CExpression ref = ref2(this.binding, onType);
        if (ref != null) {
            invocation.scope = ref;
        }
        //in pointer
        if (!clazz.isStatic && !isAbstract && !type.equals(clazz.getType()) && !isSame(this.binding, onType)) {
            invocation.isArrow = true;
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

    //is target one of my ancestors
    /*boolean isSuper(ITypeBinding from, ITypeBinding target) {
        if (from == null || from.getSuperclass() == null) {
            return false;
        }
        if (from.getSuperclass().equals(target)) {
            return true;
        }
        return isSuper(from.getSuperclass(), target);
    }*/

    public List<CExpression> list(List<Expression> arguments) {
        List<CExpression> list = new ArrayList<>();
        for (Expression expression : arguments) {
            list.add((CExpression) visitExpr(expression, null));
        }
        return list;
    }

    CNode resolveName(SimpleName node) {
        IBinding binding = node.resolveBinding();
        CName name = new CName(node.getIdentifier());
        if (binding == null) {
            Logger.logBinding(clazz, node.toString());
            return name;
        }
        if (binding.getKind() != IBinding.VARIABLE) {
            return name;
        }
        boolean isStatic = Modifier.isStatic(binding.getModifiers());
        IVariableBinding variableBinding = (IVariableBinding) binding;
        ITypeBinding onType = variableBinding.getDeclaringClass();
        if (variableBinding.isField()) {
            name = Mapper.instance.mapFieldName(name.name, clazz);
            CType type = TypeVisitor.fromBinding(onType, clazz);
            if (isStatic) {
                //static field,qualify
                if (Config.static_field_cofui && Modifier.isFinal(binding.getModifiers()) && !variableBinding.getType().isPrimitive()) {
                    return new CMethodInvocation(type, name, false);
                }
                return new CFieldAccess(type, name, false);
            }
            if (!this.binding.equals(onType) && this.binding.isSubTypeCompatible(onType)) {
                //super field?
                return name;
            }
            CExpression ref = ref2(this.binding, onType);
            if (ref != null) {
                return new CFieldAccess(ref, name, true);
            }
        }
        if (variableBinding.isParameter() || !variableBinding.isField()) {
            if (clazz.isAnonymous && onType == null) {
                CType type = TypeVisitor.fromBinding(variableBinding.getType());
                //access to local variable from anony class
                //make field and setter
                for (CField field : clazz.fields) {
                    if (field.name.is(name.name) && field.type.equals(type)) {
                        //already added
                        return name;
                    }
                }
                CField field = new CField();
                field.setType(type);
                field.setName(name);
                clazz.addField(field);
                clazz.anonyFields.add(field);
            }
            if (variableBinding.isParameter()) {
                return new CName(Mapper.instance.mapParamName(node.getIdentifier()));
            }
            //access to catch variable
            if (catchName != null && name.is(catchName) && !(node.getParent() instanceof ThrowStatement)) {
                if (node.getParent() instanceof VariableDeclarationFragment || node.getParent() instanceof Assignment || node.getParent() instanceof ClassInstanceCreation) {
                    return new ReferenceExpr(name);
                }
            }
        }
        return name;
    }

    //t1 -> t2 -> ... -> target
    //is target outer of t1
    CExpression ref2(ITypeBinding t1, ITypeBinding target) {
        ITypeBinding parent = t1.getDeclaringClass();
        if (parent == null) {
            return null;
        }
        if (parent.equals(target)) {
            return new CName(Config.parentName);
        }
        CExpression ref = ref2(parent, target);
        if (ref == null) {
            return null;
        }
        return new CFieldAccess(ref, new CName(Config.parentName), true);
    }

    @Override
    public CNode visit(SimpleName node, CNode arg) {
        return resolveName(node);
    }

    //qualified class,array.length,field access
    @Override
    public CNode visit(QualifiedName node, CNode arg) {
        IBinding binding = node.resolveBinding();
        ITypeBinding typeBinding = node.getQualifier().resolveTypeBinding();

        if ((binding instanceof IVariableBinding) && Config.writeLibHeader) {
            LibHandler.instance.addField((IVariableBinding) binding);
        }
        if (binding == null || typeBinding == null) {
            Logger.logBinding(clazz, node.toString());
            //normal qualified name access
            //qualifier is not a type so it is a package
            return new CName(node.getFullyQualifiedName());
        }

        boolean isStatic = Modifier.isStatic(binding.getModifiers());
        CType type = TypeVisitor.fromBinding(typeBinding, clazz);
        type.isPointer = false;
        CExpression scope = (CExpression) visitExpr(node.getQualifier(), arg);

        String name = node.getName().getIdentifier();

        if (isStatic) {
            if (binding.getKind() == IBinding.VARIABLE) {
                IVariableBinding variableBinding = (IVariableBinding) binding;
                if (variableBinding.isField()) {
                    if (Config.static_field_cofui && Modifier.isFinal(binding.getModifiers()) && !variableBinding.getType().isPrimitive()) {
                        return new CMethodInvocation(type, Mapper.instance.mapFieldName(name, clazz), false);
                    }
                }
            }
            return new CFieldAccess(type, Mapper.instance.mapFieldName(name, clazz), false);
        }

        if (typeBinding.isArray() && name.equals("length")) {
            IVariableBinding variableBinding = (IVariableBinding) binding;
            //array.length
            if (variableBinding.getDeclaringClass() == null) {
                return new CMethodInvocation(scope, new CName("size"), true);
            }
        }
        if (binding.getKind() == IBinding.VARIABLE) {
            IVariableBinding variableBinding = (IVariableBinding) binding;
            if (variableBinding.isField()) {
                CName mapped = Mapper.instance.mapFieldName(name, clazz);
                return new CFieldAccess(scope, mapped, true);
            }
            else if (variableBinding.isParameter()) {
                return new CFieldAccess(scope, CName.simple(Mapper.instance.mapParamName(name)), true);
            }
        }
        //qualified type
        return new CFieldAccess(scope, new CName(name), true);
    }
    
    String toWrapper(String s){
    	Map<String, String> map=new HashMap<>();
			map.put("boolean", "Boolean");
			map.put("byte", "Byte");
			map.put("char", "Character");
			map.put("short", "Short");
			map.put("int", "Integer");
			map.put("long", "Long");
			map.put("float", "Float");
			map.put("double", "Double");
			return map.get(s);
    }
    String fromWrapper(String s){
    	Map<String, String> map=new HashMap<>();
			map.put("Boolean", "boolean");
			map.put("Byte", "byte");
			map.put("Character", "char");
			map.put("Short", "short");
			map.put("Integer", "int");
			map.put("Long", "lon");
			map.put("Float", "float");
			map.put("Double", "double");
			return map.get(s);
    }
    
    
    CExpression box(Expression e, CExpression ce){
    	//Double d = i;
    	if(e.resolveBoxing()){
    		ITypeBinding t = e.resolveTypeBinding();
			String type = t.getName();
			//$Wrapper$::valueOf(e)
			CType wr = new CType("java::lang::"+toWrapper(type));
			CMethodInvocation res = new CMethodInvocation(wr, new CName("valueOf"), false);
			res.arguments.add(ce);
			return res;
    	}
    	if(e.resolveUnboxing()){
    		ITypeBinding t = e.resolveTypeBinding();
			String type = t.getName();
			//e.$prim$Value
			String prim=fromWrapper(type);
			CMethodInvocation res = new CMethodInvocation(ce, new CName(prim + "Value"), true);
			return res;
    	}
    	return ce;
	}
	
    /*String getTargetType(Expression e){
    	ASTNode p = e.getParent();
    	if(p instanceof VariableDeclarationFragment){
    		VariableDeclarationFragment f = (VariableDeclarationFragment)p;
    		return f.resolveBinding().getType().getName();
    	}
    	else if(p instanceof Assignment){
    		Assignment as = (Assignment)p;
    		return as.getLeftHandSide().resolveTypeBinding().getName();
    	}
   	 else if(p instanceof MethodInvocation){
   	 	MethodInvocation m=(MethodInvocation)p;
    	}
    	else if(p instanceof ClassInstanceCreation){
			ClassInstanceCreation cic=(ClassInstanceCreation)p;
			
		}
    	else if(p instanceof InfixExpression){
			throw new RuntimeException("infix box");
		}
    	else if(p instanceof PrefixExpression){
    		PrefixExpression pr = (PrefixExpression)p;
    		return toWrapper(e.resolveTypeBinding().getName());
		}
		else if(p instanceof CastExpression){
			CastExpression ce=(CastExpression)p;
			return ce.getType().toString();
		}
    	else if(p instanceof ArrayAccess){
    		return "int";
    	}
    	else if(p instanceof ArrayCreation){
    		return "int";
    	}
		return null;
	}*/
}
