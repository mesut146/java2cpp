package com.mesut.j2cpp.cppast.expr;

import com.mesut.j2cpp.Config;
import com.mesut.j2cpp.ast.CType;
import com.mesut.j2cpp.cppast.CExpression;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

//new Type(args)
public class CClassInstanceCreation extends CExpression {
    public CType type;
    public List<CExpression> args = new ArrayList<>();

    public void setType(CType type) {
        this.type = type.copy();
        this.type.setPointer(Config.ptr_new);
    }

    @Override
    public void print() {
        append("new ");
        append(type.toString());
        append("(");
        append(args.stream().map(CExpression::toString).collect(Collectors.joining(", ")));
        append(")");
    }
}
