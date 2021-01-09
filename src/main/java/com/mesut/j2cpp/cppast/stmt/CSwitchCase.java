package com.mesut.j2cpp.cppast.stmt;

import com.mesut.j2cpp.cppast.CExpression;
import com.mesut.j2cpp.cppast.CStatement;

public class CSwitchCase extends CStatement {
    public CExpression expression;
    public boolean isDefault = false;


    @Override
    public void print() {
        if (isDefault) {
            append("default:\n");
        }
        else {
            append("case ");
            append(expression);
            append(":\n");
        }
    }
}
