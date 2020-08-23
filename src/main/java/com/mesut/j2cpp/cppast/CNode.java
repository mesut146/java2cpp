package com.mesut.j2cpp.cppast;

import com.mesut.j2cpp.ast.Node;
import com.mesut.j2cpp.cppast.stmt.CBlockStatement;

public abstract class CNode extends Node {

    @Override
    public void print() {

    }

    public void printBody(CNode body) {
        if (body instanceof CBlockStatement) {
            append(body);
        }
        else {
            up();
            line(body.toString());
            down();
        }
    }
}
