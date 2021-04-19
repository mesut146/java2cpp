package com.mesut.j2cpp.cppast.stmt;

import com.mesut.j2cpp.cppast.CExpression;
import com.mesut.j2cpp.cppast.CStatement;
import com.mesut.j2cpp.util.PrintHelper;

import java.util.ArrayList;
import java.util.List;

public class CSwitch extends CStatement {
    public CExpression expression;
    public List<CStatement> statements = new ArrayList<>();

    @Override
    public String toString() {
        getScope(expression);
        getScope(statements);
        StringBuilder sb = new StringBuilder();
        sb.append("switch(").append(expression).append("){\n");
        for (CStatement statement : statements) {
            if (statement instanceof CCase) {
                sb.append(statement);
            }
            else {
                sb.append(PrintHelper.body(statement.toString(), "    "));
            }
            sb.append("\n");
        }
        sb.append("\n}");
        return sb.toString();
    }
}
