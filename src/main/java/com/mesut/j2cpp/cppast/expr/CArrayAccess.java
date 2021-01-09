package com.mesut.j2cpp.cppast.expr;

import com.mesut.j2cpp.cppast.CExpression;

//left[index]
public class CArrayAccess extends CExpression {
    public CExpression left;
    public CExpression index;

    @Override
    public String toString() {
        return String.format("%s->get(%s)", left, index);
    }
}
