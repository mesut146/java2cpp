package com.mesut.j2cpp.cppast.expr;

import com.mesut.j2cpp.cppast.CExpression;

//expression operator
public class CPostfixExpression extends CExpression {
    public CExpression expression;
    public String operator;

    @Override
    public String toString() {
        getScope(expression);
        return expression + operator;
    }
}
