package com.mesut.j2cpp.cppast.stmt;

import com.mesut.j2cpp.cppast.CExpression;
import com.mesut.j2cpp.cppast.CStatement;

import java.util.ArrayList;
import java.util.List;

public class CSwitchStatement extends CStatement {

    public CExpression expression;
    public List<CStatement> statements = new ArrayList<>();

    @Override
    public void print() {
        append("switch(");
        append(expression);
        appendln("){");

        if (!statements.isEmpty()) {
            for (CStatement statement : statements) {
                append(statement);
            }
        }
        line("}");
    }
}
