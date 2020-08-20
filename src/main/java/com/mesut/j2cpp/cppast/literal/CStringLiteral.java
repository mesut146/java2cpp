package com.mesut.j2cpp.cppast.literal;

import com.mesut.j2cpp.cppast.CExpression;

public class CStringLiteral extends CExpression {
    public String value;

    public CStringLiteral(String value) {
        this.value = value;
    }

    @Override
    public void print() {
        append("\"");
        append(value);
        append("\"");
    }
}
