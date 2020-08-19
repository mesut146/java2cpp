package com.mesut.j2cpp.cppast.stmt;

import com.mesut.j2cpp.cppast.CExpression;
import com.mesut.j2cpp.cppast.CStatement;

public class CSwitchStatement extends CStatement {

    public CExpression expression;

    @Override
    public void print() {
        append("switch(");
        append(expression.toString());
        appendln("){");

        line("}");
    }
}
