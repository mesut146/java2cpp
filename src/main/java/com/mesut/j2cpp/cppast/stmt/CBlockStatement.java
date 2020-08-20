package com.mesut.j2cpp.cppast.stmt;

import com.mesut.j2cpp.cppast.CStatement;

import java.util.ArrayList;
import java.util.List;

/*
{
 stmt1
 stmt2
 ....
}
 */
public class CBlockStatement extends CStatement {
    public List<CStatement> statements = new ArrayList<>();

    public void print() {
        append("{");
        up();
        for (CStatement statement : statements) {
            if (!(statement instanceof CEmptyStatement)) {
                line(statement.toString());
            }
        }
        down();
        line("}");
    }
}
