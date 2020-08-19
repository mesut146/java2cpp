package com.mesut.j2cpp.cppast.expr;

import com.mesut.j2cpp.cppast.CExpression;

public class CConditionalExpression extends CExpression {
    public CExpression condition;
    public CExpression thenExpr;
    public CExpression elseExpr;
}
