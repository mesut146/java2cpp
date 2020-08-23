package com.mesut.j2cpp.cppast;

import com.mesut.j2cpp.cppast.stmt.CBlockStatement;
import com.mesut.j2cpp.cppast.stmt.CVariableDeclarationStatement;

public class CCatchClause extends CNode {

    public CVariableDeclarationStatement expr;
    public CBlockStatement body;

    @Override
    public void print() {
        append("catch(");
        append(expr.toString());
        append(")");
        append(body.toString());
    }
}
