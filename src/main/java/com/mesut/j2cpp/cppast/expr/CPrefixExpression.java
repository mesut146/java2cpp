package com.mesut.j2cpp.cppast.expr;

import com.mesut.j2cpp.cppast.CExpression;

//operator expression
public class CPrefixExpression extends CExpression {
    public String operator;
    public CExpression expression;

    @Override
    public String toString() {
        getScope(expression);
        return operator + expression;
    }
}
