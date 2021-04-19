package com.mesut.j2cpp.cppast.stmt;

import com.mesut.j2cpp.cppast.CExpression;
import com.mesut.j2cpp.cppast.CStatement;

public class CCase extends CStatement {
    public CExpression expression;
    public boolean isDefault;

    public CCase(CExpression expression) {
        this.expression = expression;
    }

    public CCase() {
        isDefault = true;
    }

    @Override
    public String toString() {
        if (isDefault) {
            return "default:";
        }
        return "case: " + expression;
    }
}
