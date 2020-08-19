package com.mesut.j2cpp.cppast.stmt;

import com.mesut.j2cpp.cppast.CStatement;

public class CContinueStatement extends CStatement {
    public String label;

    @Override
    public void print() {
        append("continue");
        if (label != null) {
            append(" ");
            append(label);
        }
        append(";");
    }
}
