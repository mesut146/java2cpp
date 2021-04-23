package com.mesut.j2cpp.cppast.stmt;

import com.mesut.j2cpp.cppast.CExpression;
import com.mesut.j2cpp.cppast.CStatement;
import com.mesut.j2cpp.util.PrintHelper;

public class CIfStatement extends CStatement {
    public CExpression condition;
    public CStatement thenStatement;
    public CStatement elseStatement;

    public CIfStatement() {
    }

    public CIfStatement(CExpression condition, CStatement thenStatement) {
        this.condition = condition;
        this.thenStatement = thenStatement;
    }

    @Override
    public String toString() {
        getScope(condition, thenStatement, elseStatement);
        StringBuilder sb = new StringBuilder();
        sb.append("if(").append(condition).append(")\n");
        sb.append(PrintHelper.strBody(thenStatement));
        if (elseStatement != null) {
            sb.append("\nelse ");
            if (elseStatement instanceof CIfStatement) {
                sb.append(elseStatement);
            }
            else {
                sb.append(PrintHelper.strBody(elseStatement));
            }
        }
        return sb.toString();
    }
}
