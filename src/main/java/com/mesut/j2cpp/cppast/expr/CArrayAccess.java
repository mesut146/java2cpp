package com.mesut.j2cpp.cppast.expr;

import com.mesut.j2cpp.cppast.CExpression;

//left[index]
public class CArrayAccess extends CExpression {
    public CExpression left;
    public CExpression index;

    @Override
    public void print() {
        append(left.toString());
        append("->get(");
        append(index.toString());
        append(")");
    }
}
