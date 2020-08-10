package com.mesut.j2cpp.cppast.expr;

import com.mesut.j2cpp.cppast.CExpression;

//left op right
public class CAssignment extends CExpression {
    CExpression left;
    CExpression right;
    String operator;
    //op is one of = < > <= >= <<= >>=
}
