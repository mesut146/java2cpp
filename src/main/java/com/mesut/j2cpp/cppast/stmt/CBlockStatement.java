package com.mesut.j2cpp.cppast.stmt;

import com.mesut.j2cpp.cppast.CStatement;
import com.mesut.j2cpp.util.PrintHelper;

import java.util.ArrayList;
import java.util.List;

public class CBlockStatement extends CStatement {
    public List<CStatement> statements = new ArrayList<>();

    public void addStatement(CStatement statement) {
        statements.add(statement);
    }

    public void addStatement(int i, CStatement statement) {
        statements.add(i, statement);
    }

    @Override
    public String toString() {
        getScope(statements);
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        for (CStatement statement : statements) {
            if (!(statement instanceof CEmptyStatement)) {
                sb.append(PrintHelper.body(statement.toString(), "    "));
            }
        }
        sb.append("}");
        return sb.toString();
    }
}
