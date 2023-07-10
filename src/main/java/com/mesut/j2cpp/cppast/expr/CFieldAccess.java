package com.mesut.j2cpp.cppast.expr;

import com.mesut.j2cpp.ast.CName;
import com.mesut.j2cpp.cppast.CExpression;

public class CFieldAccess extends CExpression {
    public CExpression scope;
    public CName name;
    public boolean isArrow;

    public CFieldAccess(CExpression scope, CName name, boolean isArrow) {
        this.scope = scope;
        this.name = name;
        this.isArrow = isArrow;
    }

    public CFieldAccess() {
    }

    @Override
    public String toString() {
        getScope(name);
        scope.scope = super.scope;
        if (isArrow) {
            return scope + "->" + name;
        }
        else {
            return scope + "::" + name;
        }
    }
}
