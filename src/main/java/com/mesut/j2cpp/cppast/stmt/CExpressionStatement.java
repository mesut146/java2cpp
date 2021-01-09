package com.mesut.j2cpp.cppast.stmt;

import com.mesut.j2cpp.cppast.CExpression;
import com.mesut.j2cpp.cppast.CStatement;

public class CExpressionStatement extends CStatement {
    public CExpression expression;

    public CExpressionStatement(CExpression expression) {
        this.expression = expression;
    }

    @Override
    public void print() {
        append(expression);
        append(";");
    }

    @Override
    public String toString() {
        getScope(expression);
        return expression + ";";
    }
}
