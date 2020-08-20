package com.mesut.j2cpp.visitor;

import com.mesut.j2cpp.Converter;
import com.mesut.j2cpp.Helper;
import com.mesut.j2cpp.ast.*;
import com.mesut.j2cpp.cppast.*;
import com.mesut.j2cpp.cppast.expr.*;
import com.mesut.j2cpp.cppast.literal.*;
import com.mesut.j2cpp.cppast.stmt.*;
import org.eclipse.jdt.core.dom.*;

import java.util.List;

@SuppressWarnings("unchecked")
public class SourceVisitor extends GenericVisitor<CNode, CNode> {

    Converter converter;
    CSource source;
    TypeVisitor typeVisitor;
    CClass clazz;
    CMethod method;

    public SourceVisitor(Converter converter, CSource source) {
        this.converter = converter;
        this.source = source;
        typeVisitor = new TypeVisitor(converter, source.header);
    }


    public void convert() {
        for (CClass clazz : source.header.classes) {
            this.clazz = clazz;
            for (CField field : clazz.fields) {
                if (field.node == null) {
                    //enum args

                }
                else {
                    Expression expression = field.node.getInitializer();
                    if (expression != null) {
                        CFieldDef fieldDef = new CFieldDef(field);
                        fieldDef.setExpression((CExpression) visit(expression, null));
                        source.fieldDefs.add(fieldDef);
                    }
                }

            }
            for (CMethodDecl methodDecl : clazz.methods) {
                if (methodDecl.node.getBody() != null) {
                    CMethod methodDef = new CMethod();
                    methodDef.decl = methodDecl;
                    this.method = methodDef;
                    methodDef.body = (CBlockStatement) visit(methodDecl.node.getBody(), null);
                    source.methods.add(methodDef);
                }
            }
        }
    }

    @Override
    public CNode visit(Block n, CNode arg) {
        CBlockStatement res = new CBlockStatement();
        for (Statement statement : (List<Statement>) n.statements()) {
            CStatement cStatement = (CStatement) visit(statement, arg);
            if (!(cStatement instanceof CEmptyStatement)) {
                res.statements.add(cStatement);
            }
        }
        return res;
    }

    @Override
    public CNode visit(AssertStatement node, CNode arg) {
        return new CEmptyStatement();
    }

    @Override
    public CNode visit(CompilationUnit node, CNode arg) {
        return null;
    }

    @Override
    public CNode visit(PackageDeclaration node, CNode arg) {
        return null;
    }

    @Override
    public CNode visit(ImportDeclaration node, CNode arg) {
        return null;
    }

    @Override
    public CNode visit(TypeDeclaration node, CNode arg) {
        return null;
    }

    @Override
    public CNode visit(EnumDeclaration node, CNode arg) {
        return null;
    }

    @Override
    public CNode visit(FieldDeclaration node, CNode arg) {
        return null;
    }

    @Override
    public CNode visit(MethodDeclaration node, CNode arg) {
        return null;
    }

    @Override
    public CNode visit(StringLiteral node, CNode arg) {
        CObjectCreation objectCreation = new CObjectCreation();
        objectCreation.type = Helper.getStringType();
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
        CType type = typeVisitor.visitType(node.getType(), clazz);
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
        assignment.left = (CExpression) visit(node.getLeftHandSide(), arg);
        assignment.operator = node.getOperator().toString();
        assignment.right = (CExpression) visit(node.getRightHandSide(), arg);
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
        return new CReturnStatement((CExpression) visit(node.getExpression(), arg));
    }

    @Override
    public CNode visit(IfStatement node, CNode arg) {
        CIfStatement ifStatement = new CIfStatement();
        ifStatement.condition = (CExpression) visit(node.getExpression(), arg);
        ifStatement.thenStatement = (CStatement) visit(node.getThenStatement(), arg);
        if (node.getElseStatement() != null) {
            ifStatement.elseStatement = (CStatement) visit(node.getElseStatement(), arg);
        }
        return ifStatement;
    }


    @Override
    public CNode visit(ForStatement node, CNode arg) {
        CForStatement forStatement = new CForStatement();
        for (Expression init : (List<Expression>) node.initializers()) {
            forStatement.initializers.add((CExpression) visit(init, arg));
        }
        forStatement.condition = (CExpression) visit(node.getExpression(), arg);
        for (Expression updater : (List<Expression>) node.updaters()) {
            forStatement.updaters.add((CExpression) visit(updater, arg));
        }
        forStatement.body = (CStatement) visit(node.getBody(), arg);
        return forStatement;
    }

    @Override
    public CNode visit(EnhancedForStatement node, CNode arg) {
        CForEachStatement forEachStatement = new CForEachStatement();
        forEachStatement.body = (CStatement) visit(node.getBody(), arg);
        forEachStatement.left = (CSingleVariableDeclaration) visit(node.getParameter(), arg);
        forEachStatement.right = (CExpression) visit(node.getExpression(), arg);
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
        whileStatement.expression = (CExpression) visit(node.getExpression(), arg);
        whileStatement.statement = (CStatement) visit(node.getBody(), arg);
        return whileStatement;
    }

    @Override
    public CNode visit(DoStatement node, CNode arg) {
        CDoStatement doStatement = new CDoStatement();
        doStatement.body = (CStatement) visit(node.getBody(), arg);
        doStatement.expression = (CExpression) visit(node.getExpression(), arg);
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
            methodInvocation.scope = (CExpression) visit(scope, arg);
            IMethodBinding binding = node.resolveMethodBinding();
            ITypeBinding typeBinding = binding.getDeclaringClass();


            if (scope instanceof Name) {//field or class or variable
                if (Modifier.isStatic(binding.getModifiers())) {
                    methodInvocation.isArrow = false;
                    methodInvocation.scope = new CType(typeBinding.getQualifiedName());
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
            methodInvocation.arguments.add((CExpression) visit(expression, null));
        }
    }

    @Override
    public CNode visit(SwitchStatement node, CNode arg) {
        Expression expression = node.getExpression();
        ITypeBinding binding = expression.resolveTypeBinding();
        if (binding.isPrimitive()) {
            //normal switch
            CSwitchStatement switchStatement = new CSwitchStatement();
            switchStatement.expression = (CExpression) visit(expression, null);
            for (Statement statement : (List<Statement>) node.statements()) {
                if (statement instanceof SwitchCase) {
                    SwitchCase switchCase = (SwitchCase) statement;
                    CSwitchCase res = new CSwitchCase();
                    if (switchCase.isDefault()) {
                        res.isDefault = true;
                    }
                    else {
                        Expression expr = switchCase.getExpression();
                        //expr must be const-expr
                        if (expr instanceof Name || expr instanceof NumberLiteral) {
                            Object val = expr.resolveConstantExpressionValue();
                            res.expression = (CExpression) visit(expr, null);
                        }
                        else {
                            throw new RuntimeException("case must have const-expr: " + expr);
                        }

                    }
                    switchStatement.statements.add(res);
                }
                else {
                    //normal statement
                    switchStatement.statements.add((CStatement) visit(statement, null));
                }
            }
            return switchStatement;
        }
        else if (binding.isEnum()) {
            //ordinals
            CSwitchStatement switchStatement = new CSwitchStatement();
            //var->ordinal
            CMethodInvocation methodInvocation = new CMethodInvocation();
            methodInvocation.isArrow = true;
            methodInvocation.scope = (CExpression) visit(expression, null);
            methodInvocation.name = new CName("ordinal");
            switchStatement.expression = methodInvocation;

            for (Statement statement : (List<Statement>) node.statements()) {

            }
            return switchStatement;
        }
        else {
            //multiple if elses
            for (SwitchCase switchCase : (List<SwitchCase>) node.statements()) {

            }
        }
        return null;
    }

    @Override
    public CNode visit(LabeledStatement node, CNode arg) {
        return new CLabeledStatement(node.getLabel().getIdentifier());
    }

    @Override
    public CNode visit(ExpressionStatement node, CNode arg) {
        return new CExpressionStatement((CExpression) visit(node.getExpression(), arg));
    }

    @Override
    public CNode visit(ConstructorInvocation node, CNode arg) {
        Call thisCall = new Call();
        for (Expression ar : (List<Expression>) node.arguments()) {
            thisCall.args.add((CExpression) visit(ar, arg));
        }
        thisCall.isThis = true;
        method.thisCall = thisCall;
        return new CEmptyStatement();
    }

    @Override
    public CNode visit(SuperConstructorInvocation node, CNode arg) {
        Call superCall = new Call();
        superCall.isThis = false;
        for (Expression ar : (List<Expression>) node.arguments()) {
            superCall.args.add((CExpression) visit(ar, arg));
        }
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
        return new CThrowStatement((CExpression) visit(node.getExpression(), arg));
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
    public CNode visit(TypeDeclarationStatement node, CNode arg) {
        return null;
    }

    @Override
    public CNode visit(VariableDeclarationStatement node, CNode arg) {
        //split single or keep multi?
        CVariableDeclaration variableDeclaration = new CVariableDeclaration();
        CType type = typeVisitor.visitType(node.getType(), clazz);
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
            fragment.initializer = (CExpression) visit(frag.getInitializer(), arg);

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
        fieldAccess.scope = (CExpression) visit(scope, arg);

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
            classInstanceCreation.type = typeVisitor.visitType(node.getType(), clazz);
            classInstanceCreation.type.isPointer = false;
            for (Expression expression : (List<Expression>) node.arguments()) {
                classInstanceCreation.args.add((CExpression) visit(expression, null));
            }
            return classInstanceCreation;
        }
        else {
            System.out.printf("anony = %s %s\n", clazz.name, method != null ? method.getName() + "()" : "field");
            CClass anony = new CClass();
            anony.name = clazz.getAnonyName();
            anony.base.add(typeVisitor.visitType(node.getType(), clazz));
            AnonymousClassDeclaration declaration = node.getAnonymousClassDeclaration();
            DeclarationVisitor declarationVisitor = new DeclarationVisitor(this, typeVisitor);
            for (BodyDeclaration body : (List<BodyDeclaration>) declaration.bodyDeclarations()) {
                if (body instanceof FieldDeclaration) {
                    //throw new RuntimeException("anonymous field");
                    //declarationVisitor.visit((FieldDeclaration) body, anony);
                }
                else if (body instanceof MethodDeclaration) {
                    CMethod method = (CMethod) declarationVisitor.visit((MethodDeclaration) body, anony);
                    anony.methods.add(method.decl);
                }
                else {
                    throw new RuntimeException("ClassInstanceCreation anony");
                }
            }
            CClassInstanceCreation classInstanceCreation = new CClassInstanceCreation();
            classInstanceCreation.type = new CType(anony.name);
            source.anony.add(new CClassImpl(anony));
            return classInstanceCreation;
        }
    }

    @Override
    public CNode visit(ConditionalExpression node, CNode arg) {
        CTernaryExpression ternaryExpression = new CTernaryExpression();
        ternaryExpression.condition = (CExpression) visit(node.getExpression(), arg);
        ternaryExpression.thenExpr = (CExpression) visit(node.getThenExpression(), arg);
        ternaryExpression.elseExpr = (CExpression) visit(node.getElseExpression(), arg);
        return ternaryExpression;
    }

    @Override
    public CNode visit(Name node, CNode arg) {
        return new CName(node.getFullyQualifiedName());
    }

    @Override
    public CNode visit(SuperMethodInvocation node, CNode arg) {
        return null;//todo
    }

    @Override
    public CNode visit(SwitchExpression node, CNode arg) {
        return null;
    }

    @Override
    public CNode visit(ThisExpression node, CNode arg) {
        return new CThisExpression();
    }

    @Override
    public CNode visit(VariableDeclarationExpression node, CNode arg) {
        return null;
    }

    @Override
    public CNode visit(NumberLiteral node, CNode arg) {
        CNumberLiteral numberLiteral = new CNumberLiteral();
        numberLiteral.value = node.getToken();
        return numberLiteral;
    }

    @Override
    public CNode visit(SuperFieldAccess node, CNode arg) {
        return null;
    }

    @Override
    public CNode visit(InfixExpression node, CNode arg) {
        CInfixExpression infixExpression = new CInfixExpression();
        infixExpression.operator = node.getOperator().toString();
        infixExpression.left = (CExpression) visit(node.getLeftOperand(), arg);
        infixExpression.right = (CExpression) visit(node.getRightOperand(), arg);
        return infixExpression;
    }

    @Override
    public CNode visit(PostfixExpression node, CNode arg) {
        CPostfixExpression postfixExpression = new CPostfixExpression();
        postfixExpression.operator = node.getOperator().toString();
        postfixExpression.expression = (CExpression) visit(node.getOperand(), arg);
        return postfixExpression;
    }

    @Override
    public CNode visit(PrefixExpression node, CNode arg) {
        CPrefixExpression prefixExpression = new CPrefixExpression();
        prefixExpression.operator = node.getOperator().toString();
        prefixExpression.expression = (CExpression) visit(node.getOperand(), arg);
        return prefixExpression;
    }

    @Override
    public CNode visit(InstanceofExpression node, CNode arg) {
        source.hasRuntime = true;
        CMethodInvocation invocation = new CMethodInvocation();
        invocation.arguments.add((CExpression) visit(node.getLeftOperand(), arg));
        invocation.name = new CName("instance_of");
        CType type = typeVisitor.visitType(node.getRightOperand(), clazz);
        invocation.name.typeArgs.add(type);
        return invocation;
    }

    @Override
    public CNode visit(ArrayAccess node, CNode arg) {
        CArrayAccess arrayAccess = new CArrayAccess();
        arrayAccess.left = (CExpression) visit(node.getArray(), arg);
        arrayAccess.index = (CExpression) visit(node.getIndex(), arg);
        return arrayAccess;
    }

    @Override
    public CNode visit(ArrayCreation node, CNode arg) {
        if (node.getInitializer() != null) {
            return visit(node.getInitializer(), arg);
        }
        else {
            CArrayCreation2 arrayCreation2 = new CArrayCreation2();
            CType typeName = typeVisitor.visitType(node.getType(), clazz).copy();
            typeName.dimensions = node.dimensions().size();
            arrayCreation2.type = typeName;
            return arrayCreation2;
        }
    }

    @Override
    public CNode visit(CastExpression node, CNode arg) {
        CCastExpression castExpression = new CCastExpression();
        castExpression.expression = (CExpression) visit(node.getExpression(), arg);
        castExpression.targetType = typeVisitor.visitType(node.getType(), clazz);
        return castExpression;
    }

    @Override
    public CNode visit(ParenthesizedExpression node, CNode arg) {
        CParenthesizedExpression parenthesizedExpression = new CParenthesizedExpression();
        parenthesizedExpression.expression = (CExpression) visit(node.getExpression(), arg);
        return parenthesizedExpression;
    }

    //{...}
    @Override
    public CNode visit(ArrayInitializer node, CNode arg) {
        CArrayInitializer initializer = new CArrayInitializer();
        for (Expression expression : (List<Expression>) node.expressions()) {
            initializer.expressions.add((CExpression) visit(expression, arg));
        }
        return initializer;
    }

    @Override
    public CNode visit(ArrayType node, CNode arg) {
        return typeVisitor.visitType(node.getElementType(), clazz);
    }

    @Override
    public CNode visit(PrimitiveType node, CNode arg) {
        return typeVisitor.visit(node);
    }

    @Override
    public CNode visit(QualifiedType node, CNode arg) {
        return typeVisitor.visit(node);
    }

    @Override
    public CNode visit(SimpleType node, CNode arg) {
        return typeVisitor.visit(node);
    }

    CNode resolvedName(SimpleName node) {
        ITypeBinding binding = node.resolveTypeBinding();
        if (binding != null) {
            if (binding.isClass()) {
            }
            CType type = typeVisitor.fromBinding(binding);
            return type;
        }
        return new CName(node.getIdentifier());
    }


    @Override
    public CNode visit(SimpleName node, CNode arg) {
        return new CName(node.getIdentifier());
    }

    @Override
    public CNode visit(QualifiedName node, CNode arg) {
        IBinding binding = node.resolveBinding();
        if (Modifier.isStatic(binding.getModifiers())) {
            CFieldAccess fieldAccess = new CFieldAccess();
            fieldAccess.name = new CName(node.getName().getIdentifier());
            fieldAccess.isArrow = false;
            fieldAccess.scope = new CName(node.getQualifier().toString());
            return fieldAccess;
        }
        return new CName(node.getFullyQualifiedName());
    }
}
