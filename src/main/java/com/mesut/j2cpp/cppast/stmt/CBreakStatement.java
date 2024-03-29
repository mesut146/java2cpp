package com.mesut.j2cpp.cppast.stmt;

import com.mesut.j2cpp.cppast.CStatement;

public class CBreakStatement extends CStatement {
    public String label;

    public CBreakStatement(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return String.format("break %s;", label == null ? "" : label);
    }
}
