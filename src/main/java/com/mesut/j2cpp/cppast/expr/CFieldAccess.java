package com.mesut.j2cpp.cppast.expr;

import com.mesut.j2cpp.ast.CName;
import com.mesut.j2cpp.cppast.CExpression;

public class CFieldAccess extends CExpression {
    public CExpression scope;
    public CName name;
    public boolean isArrow;

    @Override
    public String toString() {
        getScope(scope, name);
        if (isArrow) {
            return scope + "->" + name;
        }
        else {
            return scope + "::" + name;
        }
    }
}
