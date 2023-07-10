package com.mesut.j2cpp.cppast.literal;

import com.mesut.j2cpp.cppast.CExpression;

public class CBooleanLiteral extends CExpression {
    public boolean value;

    public CBooleanLiteral(boolean value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
