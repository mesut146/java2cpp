package com.mesut.j2cpp.cppast;

public class ReferenceExpr extends CExpression{
    CExpression expr;

    public ReferenceExpr(CExpression expr) {
        this.expr = expr;
    }

    @Override
    public String toString() {
        getScope(expr);
        return "&" + expr;
    }
}
