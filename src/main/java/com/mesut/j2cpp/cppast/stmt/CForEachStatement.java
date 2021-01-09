package com.mesut.j2cpp.cppast.stmt;

import com.mesut.j2cpp.cppast.CExpression;
import com.mesut.j2cpp.cppast.CStatement;

public class CForEachStatement extends CStatement {
    public CStatement body;
    public CSingleVariableDeclaration left;//parameter
    public CExpression right;//expression

    @Override
    public String toString() {
        getScope(left, right, body);
        return String.format("for(%s : %s)%s", left, right, body);
    }
}
