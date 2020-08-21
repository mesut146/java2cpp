package com.mesut.j2cpp.cppast.stmt;

import com.mesut.j2cpp.cppast.CExpression;
import com.mesut.j2cpp.cppast.CStatement;

public class CIfStatement extends CStatement {
    public CExpression condition;
    public CStatement thenStatement;
    public CStatement elseStatement;

    @Override
    public void print() {
        clear();
        append("if(");
        append(condition.toString());
        append(")");
        if (thenStatement instanceof CBlockStatement) {
            append(thenStatement);
        }
        else {
            up();
            //thenStatement.setFrom(this);
            line(thenStatement.toString());
            down();
        }
        if (elseStatement != null) {
            line("else ");
            elseStatement.firstBlock = true;
            appendIndent(elseStatement);
        }
    }
}
