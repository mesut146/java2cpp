package com.mesut.j2cpp.cppast.stmt;

import com.mesut.j2cpp.cppast.CExpression;
import com.mesut.j2cpp.cppast.CStatement;

public class CDoStatement extends CStatement {
    public CExpression expression;
    public CStatement body;

    @Override
    public String toString() {
        getScope(expression, body);
        return String.format("do%s\nwhile(%s);", body, expression);
    }
}
