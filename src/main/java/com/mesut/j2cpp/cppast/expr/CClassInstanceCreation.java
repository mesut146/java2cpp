package com.mesut.j2cpp.cppast.expr;

import com.mesut.j2cpp.Config;
import com.mesut.j2cpp.ast.CType;
import com.mesut.j2cpp.cppast.CExpression;
import com.mesut.j2cpp.util.PrintHelper;

import java.util.ArrayList;
import java.util.List;

//new Type(args)
public class CClassInstanceCreation extends CExpression {
    public CType type;
    public List<CExpression> args = new ArrayList<>();

    public void setType(CType type) {
        this.type = type.copy();
        this.type.setPointer(Config.ptr_new);
    }

    @Override
    public String toString() {
        getScope(type);
        getScope(args);
        return "new " + type + "(" + PrintHelper.joinStr(args, ", ") + ")";
    }
}
