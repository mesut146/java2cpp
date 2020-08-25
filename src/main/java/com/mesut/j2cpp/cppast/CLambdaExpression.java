package com.mesut.j2cpp.cppast;

import com.mesut.j2cpp.ast.CType;
import com.mesut.j2cpp.cppast.stmt.CBlockStatement;

public class CLambdaExpression extends CExpression {

    public boolean byReference = true;
    public CType rtype;
    public CBlockStatement body;

    @Override
    public void print() {
        append("[");
        if (byReference) {
            append("&");
        }
        append("]");
        if (rtype != null) {
            append("()");//no arguments
            append(" -> ");
            append(rtype.toString());
        }
        printBody(body);
    }
}
