package com.mesut.j2cpp.cppast.expr;

import com.mesut.j2cpp.cppast.CExpression;

//cond ? val : val2
public class CTernaryExpression extends CExpression {
    public CExpression condition;
    public CExpression thenExpr;
    public CExpression elseExpr;

    @Override
    public String toString() {
        getScope(condition, thenExpr, elseExpr);
        return String.format("%s ? %s : %s", condition, thenExpr, elseExpr);
    }
}
