package com.mesut.j2cpp.cppast.expr;

import com.mesut.j2cpp.cppast.CExpression;

//left op right
public class CAssignment extends CExpression {
    public CExpression left;
    public CExpression right;
    public String operator;
    //op is one of = < > <= >= <<= >>=


    public CAssignment() {
    }

    public CAssignment(CExpression left, CExpression right, String operator) {
        this.left = left;
        this.right = right;
        this.operator = operator;
    }

    @Override
    public String toString() {
        return left + " " + operator + " " + right;
    }

}
