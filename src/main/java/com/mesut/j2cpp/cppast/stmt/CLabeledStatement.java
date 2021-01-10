package com.mesut.j2cpp.cppast.stmt;

import com.mesut.j2cpp.cppast.CStatement;

public class CLabeledStatement extends CStatement {
    public String label;

    public CLabeledStatement(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label + ":";
    }
}
