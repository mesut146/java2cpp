package com.mesut.j2cpp.cppast.stmt;

import com.mesut.j2cpp.cppast.CStatement;

public class CContinueStatement extends CStatement {
    public String label;

    @Override
    public String toString() {
        return String.format("continue %s;", label == null ? ":" : label);
    }
}
