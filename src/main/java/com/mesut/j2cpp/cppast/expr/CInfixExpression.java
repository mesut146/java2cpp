package com.mesut.j2cpp.cppast.expr;

import com.mesut.j2cpp.cppast.CExpression;

// a + b
public class CInfixExpression extends CExpression {
    public CExpression left;
    public CExpression right;
    public String operator;

    public CInfixExpression() {
    }

    public CInfixExpression(CExpression left, CExpression right, String operator) {
        this.left = left;
        this.right = right;
        this.operator = operator;
    }

    @Override
    public void print() {
        append(left.toString());
        append(" ");
        append(operator);
        append(" ");
        append(right.toString());
    }
}
