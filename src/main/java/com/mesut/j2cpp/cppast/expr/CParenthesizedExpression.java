package com.mesut.j2cpp.cppast.expr;

import com.mesut.j2cpp.cppast.CExpression;

// ( expr )
public class CParenthesizedExpression extends CExpression {
    public CExpression expression;

    public CParenthesizedExpression(CExpression expression) {
        this.expression = expression;
    }

    @Override
    public String toString() {
        getScope(expression);
        return "(" + expression + ")";
    }
}
