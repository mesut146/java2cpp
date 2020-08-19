package com.mesut.j2cpp.cppast.stmt;

import com.mesut.j2cpp.cppast.CExpression;
import com.mesut.j2cpp.cppast.CStatement;

public class CForEachStatement extends CStatement {
    public CStatement body;
    public CSingleVariableDeclaration left;//parameter
    public CExpression right;//expression


    @Override
    public void print() {
        append("for(");
        append(left.toString());
        append(" : ");
        append(right.toString());
        append(")");
        append(body.toString());
    }
}
