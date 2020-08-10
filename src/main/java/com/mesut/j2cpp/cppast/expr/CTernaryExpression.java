package com.mesut.j2cpp.cppast.expr;

import com.mesut.j2cpp.cppast.CExpression;

import java.security.cert.Certificate;

//cond ? val : val2
public class CTernaryExpression extends CExpression {
    CExpression condition;
    CExpression expression1;
    CExpression expression2;
}
