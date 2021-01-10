package com.mesut.j2cpp.cppast.stmt;

import com.mesut.j2cpp.cppast.CCatchClause;
import com.mesut.j2cpp.cppast.CStatement;
import com.mesut.j2cpp.util.PrintHelper;

import java.util.ArrayList;
import java.util.List;

public class CTryStatement extends CStatement {
    public CBlockStatement body;
    public List<CCatchClause> catchClauses = new ArrayList<>();

    @Override
    public String toString() {
        getScope(body);
        getScope(catchClauses);
        return "try" + PrintHelper.body(body.toString(), "    ") + PrintHelper.join(catchClauses, "\n", scope);
    }
}
