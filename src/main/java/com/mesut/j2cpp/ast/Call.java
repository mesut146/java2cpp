package com.mesut.j2cpp.ast;


import com.mesut.j2cpp.cppast.CExpression;
import com.mesut.j2cpp.cppast.CNode;
import com.mesut.j2cpp.util.PrintHelper;

import java.util.ArrayList;
import java.util.List;

//super call in cons
public class Call extends CNode {
    public boolean isThis;
    public CType type;//this class or base class
    public List<CExpression> args = new ArrayList<>();

    @Override
    public String toString() {
        getScope(type);
        getScope(args);
        return String.format("%s (%s)", type, PrintHelper.joinStr(args, ", "));
    }
}
