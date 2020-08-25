package com.mesut.j2cpp.cppast.stmt;

import com.mesut.j2cpp.cppast.CCatchClause;
import com.mesut.j2cpp.cppast.CStatement;

import java.util.ArrayList;
import java.util.List;

public class CTryStatement extends CStatement {
    public CBlockStatement body;
    public List<CCatchClause> catchClauses = new ArrayList<>();

    @Override
    public void print() {
        append("try");
        printBody(body);
        for (CCatchClause cc : catchClauses) {
            append(cc);
        }
    }
}
