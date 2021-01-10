package com.mesut.j2cpp.cppast.expr;

import com.mesut.j2cpp.ast.CType;
import com.mesut.j2cpp.cppast.CExpression;
import com.mesut.j2cpp.util.PrintHelper;

import java.util.ArrayList;
import java.util.List;

//new type(args)
public class CObjectCreation extends CExpression {
    public CType type;
    public List<CExpression> args = new ArrayList<>();

    @Override
    public String toString() {
        getScope(type);
        getScope(args);
        return String.format("new %s(%s)", type, PrintHelper.join(args, ", ", scope));
    }
}
