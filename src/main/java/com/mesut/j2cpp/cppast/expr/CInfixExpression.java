package com.mesut.j2cpp.cppast.expr;

import com.mesut.j2cpp.cppast.CExpression;

// a + b
public class CInfixExpression extends CExpression {
    CExpression left;
    CExpression right;
    String operator;
}
