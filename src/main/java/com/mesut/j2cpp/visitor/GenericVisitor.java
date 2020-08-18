package com.mesut.j2cpp.visitor;

import org.eclipse.jdt.core.dom.*;

import java.util.List;

@SuppressWarnings("unchecked")
public abstract class GenericVisitor<R, A> implements Visitor<R, A> {


    public R visit(Statement n, A arg) {
        if (n instanceof Block) {
            return visit((Block) n, arg);
        }
        else if (n instanceof BreakStatement) {
            return visit((BreakStatement) n, arg);
        }
        else if (n instanceof ConstructorInvocation) {
            return visit((ConstructorInvocation) n, arg);
        }
        else if (n instanceof ContinueStatement) {
            return visit((ContinueStatement) n, arg);
        }
        else if (n instanceof DoStatement) {
            return visit((DoStatement) n, arg);
        }
        else if (n instanceof EnhancedForStatement) {
            return visit((EnhancedForStatement) n, arg);
        }
        else if (n instanceof ExpressionStatement) {
            return visit((ExpressionStatement) n, arg);
        }
        else if (n instanceof ForStatement) {
            return visit((ForStatement) n, arg);
        }

        else if (n instanceof IfStatement) {
            return visit((IfStatement) n, arg);
        }
        else if (n instanceof LabeledStatement) {
            return visit((LabeledStatement) n, arg);
        }
        else if (n instanceof ReturnStatement) {
            return visit((ReturnStatement) n, arg);
        }
        else if (n instanceof SuperConstructorInvocation) {
            return visit((SuperConstructorInvocation) n, arg);
        }
        else if (n instanceof SwitchCase) {
            return visit((SwitchCase) n, arg);
        }
        else if (n instanceof SwitchStatement) {
            return visit((SwitchStatement) n, arg);
        }
        else if (n instanceof SynchronizedStatement) {
            return visit((SynchronizedStatement) n, arg);
        }
        else if (n instanceof ThrowStatement) {
            return visit((ThrowStatement) n, arg);
        }
        else if (n instanceof TryStatement) {
            return visit((TryStatement) n, arg);
        }
        else if (n instanceof TypeDeclarationStatement) {
            return visit((TypeDeclarationStatement) n, arg);
        }
        else if (n instanceof VariableDeclarationStatement) {
            return visit((VariableDeclarationStatement) n, arg);
        }
        else if (n instanceof WhileStatement) {
            return visit((WhileStatement) n, arg);
        }

        return null;
    }


    public R visit(Block n, A arg) {
        for (Statement statement : (List<Statement>) n.statements()) {
            visit(statement, arg);
        }
        return null;
    }


    //expressions
    public R visit(Expression n, A arg) {
        if (n instanceof ArrayAccess) {
            return visit((ArrayAccess) n, arg);
        }
        else if (n instanceof ArrayCreation) {
            return visit((ArrayCreation) n, arg);
        }
        else if (n instanceof ArrayInitializer) {
            return visit((ArrayInitializer) n, arg);
        }
        else if (n instanceof Assignment) {
            return visit((Assignment) n, arg);
        }
        else if (n instanceof BooleanLiteral) {
            return visit((BooleanLiteral) n, arg);
        }
        else if (n instanceof CastExpression) {
            return visit((CastExpression) n, arg);
        }

        else if (n instanceof CharacterLiteral) {
            return visit((CharacterLiteral) n, arg);
        }
        else if (n instanceof ClassInstanceCreation) {
            return visit((ClassInstanceCreation) n, arg);
        }
        else if (n instanceof ConditionalExpression) {
            return visit((ConditionalExpression) n, arg);
        }
        else if (n instanceof InfixExpression) {
            return visit((InfixExpression) n, arg);
        }
        else if (n instanceof InstanceofExpression) {
            return visit((InstanceofExpression) n, arg);
        }
        else if (n instanceof Name) {
            return visit((Name) n, arg);
        }
        else if (n instanceof NullLiteral) {
            return visit((NullLiteral) n, arg);
        }
        else if (n instanceof NumberLiteral) {
            return visit((NumberLiteral) n, arg);
        }
        else if (n instanceof ParenthesizedExpression) {
            return visit((ParenthesizedExpression) n, arg);
        }
        else if (n instanceof PostfixExpression) {
            return visit((PostfixExpression) n, arg);
        }
        else if (n instanceof PrefixExpression) {
            return visit((PrefixExpression) n, arg);
        }
        else if (n instanceof StringLiteral) {
            return visit((StringLiteral) n, arg);
        }
        else if (n instanceof SuperFieldAccess) {
            return visit((SuperFieldAccess) n, arg);
        }
        else if (n instanceof SuperMethodInvocation) {
            return visit((SuperMethodInvocation) n, arg);
        }
        else if (n instanceof SwitchExpression) {
            return visit((SwitchExpression) n, arg);
        }
        else if (n instanceof ThisExpression) {
            return visit((ThisExpression) n, arg);
        }
        else if (n instanceof TypeLiteral) {
            return visit((TypeLiteral) n, arg);
        }
        else if (n instanceof VariableDeclarationExpression) {
            return visit((VariableDeclarationExpression) n, arg);
        }

        return null;
    }


}
