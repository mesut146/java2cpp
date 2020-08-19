package com.mesut.j2cpp.cppast.expr;

import com.mesut.j2cpp.ast.CType;
import com.mesut.j2cpp.cppast.CExpression;

public class CArrayCreation2 extends CExpression {

    public CType type;

    @Override
    public void print() {
        append("new ");
        append(type);
        append("(");
        append(")");
    }
}
