package com.mesut.j2cpp.cppast.expr;

import com.mesut.j2cpp.ast.CType;
import com.mesut.j2cpp.cppast.CExpression;
import com.mesut.j2cpp.util.PrintHelper;

import java.util.ArrayList;
import java.util.List;

//new ns::Type(args)
public class CObjectCreation extends CExpression {
    public CType type;
    public List<CExpression> args = new ArrayList<>();

    @Override
    public String toString() {
        return String.join("new %s()", type.normalized(scope), PrintHelper.join(args, ", ", scope));
    }
}
