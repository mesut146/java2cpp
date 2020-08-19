package com.mesut.j2cpp.cppast;

import com.mesut.j2cpp.cppast.stmt.CBlockStatement;
import com.mesut.j2cpp.cppast.stmt.CVariableDeclaration;

public class CCatchClause extends CNode {

    public CVariableDeclaration expr;
    public CBlockStatement body;

    @Override
    public void print() {
        append("catch(");
        append(expr.toString());
        append(")");
        append(body.toString());
    }
}
