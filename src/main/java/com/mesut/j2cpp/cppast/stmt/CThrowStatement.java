package com.mesut.j2cpp.cppast.stmt;

import com.mesut.j2cpp.cppast.CExpression;
import com.mesut.j2cpp.cppast.CStatement;

//throw expr
public class CThrowStatement extends CStatement {
    public CExpression expression;

    public CThrowStatement(CExpression expression) {
        this.expression = expression;
    }


    @Override
    public void print() {
        append("throw ");
        append(expression.toString());
        append(";");
    }
}
