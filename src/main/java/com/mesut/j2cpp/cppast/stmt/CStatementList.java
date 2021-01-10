package com.mesut.j2cpp.cppast.stmt;

import com.mesut.j2cpp.cppast.CStatement;
import com.mesut.j2cpp.util.PrintHelper;

import java.util.ArrayList;
import java.util.List;

public class CStatementList extends CStatement {
    public List<CStatement> statements = new ArrayList<>();

    @Override
    public String toString() {
        return PrintHelper.join(statements, "\n", scope);
    }
}
