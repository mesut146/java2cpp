package com.mesut.j2cpp.cppast;

public class RawStatement extends CStatement {
    String str;

    public RawStatement(String str) {
        this.str = str;
    }

    @Override
    public String toString() {
        return str;
    }
}
