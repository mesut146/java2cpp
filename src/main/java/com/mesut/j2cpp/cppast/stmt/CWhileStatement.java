package com.mesut.j2cpp.cppast.stmt;

import com.mesut.j2cpp.cppast.CExpression;
import com.mesut.j2cpp.cppast.CStatement;

public class CWhileStatement extends CStatement {
    public CExpression expression;
    public CStatement statement;

    @Override
    public void print() {
        append("while(");
        append(expression.toString());
        append(")");
        printBody(statement);
    }
}
