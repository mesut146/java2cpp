package com.mesut.j2cpp.cppast;

//left op right
public class CAssignment extends CStatement {
    CExpression left;
    CExpression right;
    String operator;
    //op is one of = < > <= >= <<= >>=
}
