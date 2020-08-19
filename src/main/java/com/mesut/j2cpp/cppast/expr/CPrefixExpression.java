package com.mesut.j2cpp.cppast.expr;

import com.mesut.j2cpp.cppast.CExpression;
import com.mesut.j2cpp.cppast.CNode;

//operator expression
public class CPrefixExpression extends CExpression {
    public String operator;
    public CExpression expression;
}
