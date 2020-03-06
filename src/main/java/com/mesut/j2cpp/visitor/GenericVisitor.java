package com.mesut.j2cpp.visitor;

import org.eclipse.jdt.core.dom.*;

public class GenericVisitor<R, A> extends ASTVisitor {

    //statements

    public R visit(Statement n, A arg) {
        return null;
    }

    public R visit(ExpressionStatement n, A arg) {
        return null;
    }

    public R visit(Block n, A arg) {
        return null;
    }

    public R visit(ForStatement n, A arg) {
        return null;
    }

    public R visit(WhileStatement n, A arg) {
        return null;
    }

    public Object visit(IfStatement n, A arg) {
        return null;
    }

    public R visit(ReturnStatement n, A arg) {
        return null;
    }

    public R visit(TryStatement n, A arg) {
        return null;
    }

    public R visit(ThrowStatement n, A arg) {
        return null;
    }

    //expressions

    public R visit(Expression n, A arg) {
        return null;
    }

    public R visit(MethodInvocation n, A arg) {
        return null;
    }

    public R visit(FieldAccess n, A arg) {
        return null;
    }

}
