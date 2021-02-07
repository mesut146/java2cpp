package com.mesut.j2cpp.visitor;

import org.eclipse.jdt.core.dom.*;


public interface Visitor<R, A> {

    R visit(CompilationUnit node, A arg);

    R visit(PackageDeclaration node, A arg);

    R visit(ImportDeclaration node, A arg);

    R visit(TypeDeclaration node, A arg);

    R visit(EnumDeclaration node, A arg);

    R visit(FieldDeclaration node, A arg);

    R visit(MethodDeclaration node, A arg);


    //literals
    R visit(StringLiteral node, A arg);

    R visit(BooleanLiteral node, A arg);

    R visit(CharacterLiteral node, A arg);

    R visit(NullLiteral node, A arg);

    R visit(TypeLiteral node, A arg);

    //statements
    R visit(Assignment node, A arg);

    R visit(Block node, A arg);

    R visit(BreakStatement node, A arg);

    R visit(ContinueStatement node, A arg);

    R visit(ReturnStatement node, A arg);

    R visit(IfStatement node, A arg);

    R visit(ForStatement node, A arg);

    R visit(EnhancedForStatement node, A arg);

    R visit(WhileStatement node, A arg);

    R visit(DoStatement node, A arg);

    R visit(YieldStatement node, A arg);

    R visit(AssertStatement node, A arg);

    R visit(MethodInvocation node, A arg);

    R visit(SwitchStatement node, A arg);

    R visit(LabeledStatement node, A arg);

    R visit(EmptyStatement node,A arg);

    R visit(ConstructorInvocation node, A arg);

    R visit(ExpressionStatement node, A arg);

    R visit(SuperConstructorInvocation node, A arg);

    R visit(SwitchCase node, A arg);

    R visit(SynchronizedStatement node, A arg);

    R visit(ThrowStatement node, A arg);

    R visit(TryStatement node, A arg);

    R visit(TypeDeclarationStatement node, A arg);

    R visit(VariableDeclarationStatement node, A arg);


    //expressions
    R visit(VariableDeclaration node, A arg);

    R visit(FieldAccess node, A arg);

    R visit(ClassInstanceCreation node, A arg);

    R visit(ConditionalExpression node, A arg);

    R visit(Name node, A arg);


    R visit(SuperMethodInvocation node, A arg);

    R visit(SingleVariableDeclaration node, A arg);

    R visit(SwitchExpression node, A arg);

    R visit(ThisExpression node, A arg);

    R visit(VariableDeclarationExpression node, A arg);

    R visit(NumberLiteral node, A arg);

    R visit(SuperFieldAccess node, A arg);

    R visit(InfixExpression node, A arg);

    R visit(PostfixExpression node, A arg);

    R visit(PrefixExpression node, A arg);

    R visit(InstanceofExpression node, A arg);

    R visit(ArrayAccess node, A arg);

    R visit(ArrayCreation node, A arg);

    R visit(CastExpression node, A arg);

    R visit(ParenthesizedExpression node, A arg);

    R visit(ArrayInitializer node, A arg);

    //types
    R visit(ArrayType node, A arg);

    R visit(PrimitiveType node, A arg);

    R visit(QualifiedType node, A arg);

    R visit(SimpleType node, A arg);


    R visit(SimpleName node, A arg);

    R visit(QualifiedName node, A arg);
}
