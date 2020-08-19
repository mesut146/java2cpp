package com.mesut.j2cpp.cppast.stmt;

import com.mesut.j2cpp.cppast.CStatement;

public class CBreakStatement extends CStatement {
    public String label;

    @Override
    public void print() {
        append("break");
        if (label != null) {
            append(" ");
            append(label);
        }
        append(";");
    }
}
