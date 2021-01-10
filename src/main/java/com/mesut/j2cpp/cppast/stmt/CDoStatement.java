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
        return String.format("do\n%s\nwhile(%s);", PrintHelper.body(body.toString(), "    "), expression);
    }
}
