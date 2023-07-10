package com.mesut.j2cpp.cppast;

public class DeferenceExpr extends CExpression {
    CExpression expression;

    public DeferenceExpr(CExpression expression) {
        this.expression = expression;
    }

    @Override
    public String toString() {
        getScope(expression);
        //todo expr may require paren
        return "*" + expression;
    }
}
