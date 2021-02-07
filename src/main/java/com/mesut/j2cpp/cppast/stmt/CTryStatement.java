package com.mesut.j2cpp.cppast.stmt;

import com.mesut.j2cpp.cppast.CCatchClause;
import com.mesut.j2cpp.cppast.CStatement;
import com.mesut.j2cpp.util.PrintHelper;

import java.util.ArrayList;
import java.util.List;

public class CTryStatement extends CStatement {
    public CBlockStatement body;
    public List<CCatchClause> catchClauses = new ArrayList<>();
    public CBlockStatement finallyBlock;

    @Override
    public String toString() {
        getScope(body);
        getScope(catchClauses);
        getScope(finallyBlock);
        StringBuilder sb = new StringBuilder();
        sb.append("try\n").append(body).append("\n");
        sb.append(PrintHelper.joinStr(catchClauses, "\n"));
        if (finallyBlock != null) {
            sb.append("//finally\n");
            sb.append(finallyBlock);
        }
        return sb.toString();
    }
}
