package com.mesut.j2cpp.cppast.expr;

import com.mesut.j2cpp.ast.CType;
import com.mesut.j2cpp.cppast.CExpression;

public class CCastExpression extends CExpression {
    public CType targetType;
    public CExpression expression;
}
