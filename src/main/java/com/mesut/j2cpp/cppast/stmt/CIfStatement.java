package com.mesut.j2cpp.cppast.stmt;

import com.mesut.j2cpp.cppast.CExpression;
import com.mesut.j2cpp.cppast.CStatement;
import com.mesut.j2cpp.util.PrintHelper;

public class CIfStatement extends CStatement {
    public CExpression condition;
    public CStatement thenStatement;
    public CStatement elseStatement;

    @Override
    public String toString() {
        getScope(condition, thenStatement, elseStatement);
        StringBuilder sb = new StringBuilder();
        sb.append("if(").append(condition).append(")\n");
        sb.append(PrintHelper.body(thenStatement.toString(), "    "));
        if (elseStatement != null) {
            sb.append("    else ");
            if (elseStatement instanceof CIfStatement) {
                sb.append(PrintHelper.body(elseStatement.toString(), "", true));
            }
            else {
                sb.append(PrintHelper.body(elseStatement.toString(), "    ", true));
            }
        }
        return sb.toString();
    }
}
