package com.mesut.j2cpp.cppast.stmt;

import com.mesut.j2cpp.cppast.CStatement;

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
        up();
        for (CStatement statement : statements) {
            if (!(statement instanceof CEmptyStatement)) {
                sb.append(getIndent()).append(statement).append("\n");
            }
        }
        sb.append("}");
        return sb.toString();
    }
}
