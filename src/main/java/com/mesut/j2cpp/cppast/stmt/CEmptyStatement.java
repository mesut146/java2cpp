package com.mesut.j2cpp.cppast.stmt;

import com.mesut.j2cpp.cppast.CStatement;

public class CEmptyStatement extends CStatement {
    @Override
    public String toString() {
        return ";";
    }
}
