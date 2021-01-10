package com.mesut.j2cpp.cppast.stmt;

import com.mesut.j2cpp.cppast.CExpression;
import com.mesut.j2cpp.cppast.CStatement;

//return exp
public class CReturnStatement extends CStatement {
    public CExpression expression;

    public CReturnStatement(CExpression expression) {
        this.expression = expression;
    }

    @Override
    public String toString() {
        getScope(expression);
        if (expression == null) {
            return "return;";
        }
        return "return " + expression + ";";
    }
}
