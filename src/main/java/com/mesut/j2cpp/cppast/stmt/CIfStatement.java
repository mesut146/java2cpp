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
        printBody(thenStatement);
        if (elseStatement != null) {
            line("else ");
            if (elseStatement instanceof CIfStatement) {
                elseStatement.firstBlock = true;
                append(elseStatement);
            }
            else {
                printBody(elseStatement);
            }

        }
    }
}
