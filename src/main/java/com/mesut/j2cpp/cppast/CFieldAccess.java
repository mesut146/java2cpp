package com.mesut.j2cpp.cppast;

import com.mesut.j2cpp.ast.CName;

public class CFieldAccess extends CExpression{
    CExpression scope;
    CName name;
    boolean isArrow;
}
