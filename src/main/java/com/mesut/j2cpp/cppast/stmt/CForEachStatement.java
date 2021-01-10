package com.mesut.j2cpp.cppast.stmt;

import com.mesut.j2cpp.cppast.CExpression;
import com.mesut.j2cpp.cppast.CStatement;
import com.mesut.j2cpp.util.PrintHelper;

public class CForEachStatement extends CStatement {
    public CStatement body;
    public CSingleVariableDeclaration left;//parameter
    public CExpression right;//expression

    @Override
    public String toString() {
        getScope(left, right, body);
        return String.format("for(%s : %s)\n%s", left, right, PrintHelper.body(body.toString(), "    "));
    }
}
