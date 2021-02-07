package com.mesut.j2cpp.cppast;

import com.mesut.j2cpp.cppast.stmt.CBlockStatement;
import com.mesut.j2cpp.cppast.stmt.CSingleVariableDeclaration;
import com.mesut.j2cpp.util.PrintHelper;

public class CCatchClause extends CNode {

    public CSingleVariableDeclaration expr;
    public CBlockStatement body;
    public boolean catchAll = false;

    @Override
    public String toString() {
        getScope(expr, body);
        StringBuilder sb = new StringBuilder();
        sb.append("catch(");
        if (catchAll) {
            sb.append("...");
        }
        else {
            sb.append(expr);
        }
        sb.append(")\n");
        sb.append(PrintHelper.strBody(body));
        return sb.toString();
    }


}
