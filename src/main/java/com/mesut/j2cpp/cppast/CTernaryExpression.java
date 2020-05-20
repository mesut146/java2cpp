package com.mesut.j2cpp.cppast;

import java.security.cert.Certificate;

//cond ? val : val2
public class CTernaryExpression extends CExpression{
    CExpression condition;
    CExpression expression1;
    CExpression expression2;
}
