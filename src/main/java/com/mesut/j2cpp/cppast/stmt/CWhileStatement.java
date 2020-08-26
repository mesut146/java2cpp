package com.mesut.j2cpp.cppast.stmt;

import com.mesut.j2cpp.cppast.CExpression;
import com.mesut.j2cpp.cppast.CStatement;

public class CWhileStatement extends CStatement {
    public CExpression expression;
    public CStatement statement;

    public void setStatement(CStatement statement) {
        this.statement = statement;
    }

    @Override
    public void print() {
        append("while(");
        append(expression.toString());
        append(")");
        if (statement == null) {
            append(";");
        }
        else {
            printBody(statement);
        }
    }
}
