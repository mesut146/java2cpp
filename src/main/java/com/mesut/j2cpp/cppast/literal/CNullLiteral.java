package com.mesut.j2cpp.cppast.literal;

import com.mesut.j2cpp.cppast.CExpression;

public class CNullLiteral extends CExpression {
    @Override
    public String toString() {
        return "nullptr";
    }
}
