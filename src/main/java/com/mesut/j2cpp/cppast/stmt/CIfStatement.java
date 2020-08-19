package com.mesut.j2cpp.cppast.stmt;

import com.mesut.j2cpp.cppast.CExpression;
import com.mesut.j2cpp.cppast.CStatement;

public class CIfStatement extends CStatement {
    public CExpression condition;
    public CStatement thenStatement;
    public CStatement elseStatement;

    @Override
    public void print() {
        append("if(");
        append(condition.toString());
        append(")");
        if (!(thenStatement instanceof CBlockStatement)) {
            append("\n");
        }
        append(thenStatement.toString());
        if (elseStatement != null) {
            append("else");
            append(elseStatement.toString());
        }
    }
}
