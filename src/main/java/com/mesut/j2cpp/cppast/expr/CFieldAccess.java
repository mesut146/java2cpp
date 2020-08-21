package com.mesut.j2cpp.cppast.expr;

import com.mesut.j2cpp.ast.CName;
import com.mesut.j2cpp.cppast.CExpression;

public class CFieldAccess extends CExpression {
    public CExpression scope;
    public CName name;
    public boolean isArrow;



    @Override
    public void print() {
        append(scope.toString());
        if (isArrow) {
            append("->");
        }
        else {
            append("::");
        }
        append(name.toString());
    }
}
