package com.mesut.j2cpp.cppast;

import com.mesut.j2cpp.cppast.stmt.CBlockStatement;
import com.mesut.j2cpp.cppast.stmt.CSingleVariableDeclaration;

public class CCatchClause extends CNode {

    public CSingleVariableDeclaration expr;
    public CBlockStatement body;
    public boolean catchAll = false;

    @Override
    public void print() {
        append("catch(");
        if (catchAll) {
            append("...");
        }
        else {
            append(expr.toString());
        }
        append(")");
        printBody(body);
    }
}
