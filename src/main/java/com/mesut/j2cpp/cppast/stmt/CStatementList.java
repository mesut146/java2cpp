package com.mesut.j2cpp.cppast.stmt;

import com.mesut.j2cpp.cppast.CStatement;

import java.util.ArrayList;
import java.util.List;

public class CStatementList extends CStatement {
    public List<CStatement> statements = new ArrayList<>();

    @Override
    public void print() {
        for (CStatement statement : statements) {
            append(statement);
        }
    }
}
