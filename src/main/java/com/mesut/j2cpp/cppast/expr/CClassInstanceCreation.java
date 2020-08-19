package com.mesut.j2cpp.cppast.expr;

import com.mesut.j2cpp.ast.CType;
import com.mesut.j2cpp.cppast.CExpression;

import java.util.ArrayList;
import java.util.List;

public class CClassInstanceCreation extends CExpression {
    public CType type;
    public List<CExpression> args = new ArrayList<>();


    @Override
    public void print() {
        append("new ");

        append(type.toString());
        append("(");
        for (int i = 0; i < args.size(); i++) {
            append(args.get(i).toString());
            if (i < args.size() - 1) {
                append(", ");
            }
        }
        append(")");
    }
}
