package com.mesut.j2cpp.cppast.stmt;

import com.mesut.j2cpp.cppast.CExpression;
import com.mesut.j2cpp.cppast.CStatement;
import com.mesut.j2cpp.util.PrintHelper;

public class CDoStatement extends CStatement {
    public CExpression expression;
    public CStatement body;

    @Override
    public String toString() {
        getScope(expression, body);
        StringBuilder sb = new StringBuilder();
        sb.append("do\n");
        sb.append(PrintHelper.body(body.toString(), "    "));
        sb.append("    ").append("while(").append(expression).append(");");
        return sb.toString();
    }
}
