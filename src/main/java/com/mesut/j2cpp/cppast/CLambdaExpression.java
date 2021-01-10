package com.mesut.j2cpp.cppast;

import com.mesut.j2cpp.ast.CType;
import com.mesut.j2cpp.cppast.stmt.CBlockStatement;
import com.mesut.j2cpp.util.PrintHelper;

public class CLambdaExpression extends CExpression {

    public boolean byReference = true;
    public CType rtype;
    public CBlockStatement body;

    @Override
    public String toString() {
        getScope(rtype, body);
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        if (byReference) {
            sb.append("&");
        }
        sb.append("]");
        if (rtype != null) {
            sb.append("()");//no arguments
            sb.append(" -> ");
            sb.append(rtype);
        }
        sb.append(PrintHelper.body(body.toString(), "    "));
        return sb.toString();
    }
}
