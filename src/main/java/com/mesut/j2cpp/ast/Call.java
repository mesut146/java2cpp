package com.mesut.j2cpp.ast;


import com.mesut.j2cpp.cppast.CExpression;
import com.mesut.j2cpp.cppast.CNode;

import java.util.ArrayList;
import java.util.List;

//super call in cons
public class Call extends CNode {
    public boolean isThis;
    public String str;
    public List<CExpression> args = new ArrayList<>();


    @Override
    public void print() {
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
