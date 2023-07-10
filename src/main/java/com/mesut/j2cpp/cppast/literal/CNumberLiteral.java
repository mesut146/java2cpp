package com.mesut.j2cpp.cppast.literal;

import com.mesut.j2cpp.cppast.CExpression;

public class CNumberLiteral extends CExpression {
    public String value;

    public CNumberLiteral(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
