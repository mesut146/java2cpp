package com.mesut.j2cpp.cppast.expr;

import com.mesut.j2cpp.cppast.CExpression;

public class CConditionalExpression extends CExpression {
    CExpression condition;
    CExpression thenExpr;
    CExpression elseExpr;
}
