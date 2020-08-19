package com.mesut.j2cpp.cppast.literal;

import com.mesut.j2cpp.cppast.CExpression;

public class CNumberLiteral extends CExpression {
    public String value;

    @Override
    public void print() {
        append(value);
    }
}
