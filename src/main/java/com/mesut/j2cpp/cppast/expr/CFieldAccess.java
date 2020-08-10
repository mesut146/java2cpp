package com.mesut.j2cpp.cppast.expr;

import com.mesut.j2cpp.ast.CName;
import com.mesut.j2cpp.cppast.CExpression;

public class CFieldAccess extends CExpression {
    CExpression scope;
    CName name;
    boolean isArrow;
}
