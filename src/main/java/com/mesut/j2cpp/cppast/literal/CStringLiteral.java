package com.mesut.j2cpp.cppast.literal;

import com.mesut.j2cpp.cppast.CExpression;

public class CStringLiteral extends CExpression {
    public String literalValue;
    public String escapedValue;

    public CStringLiteral(String value, String escapedValue) {
        this.literalValue = value;
        this.escapedValue = escapedValue;
    }

    @Override
    public String toString() {
        if (escapedValue == null) {
            return "\"" + literalValue + "\"";
        }
        return escapedValue;
    }
}
