package com.mesut.j2cpp.cppast.expr;

import com.mesut.j2cpp.ast.CName;
import com.mesut.j2cpp.cppast.CExpression;

import java.util.ArrayList;
import java.util.List;

public class CMethodInvocation extends CExpression {
    public CExpression scope;
    public CName name;
    public List<CExpression> arguments = new ArrayList<>();
    public boolean isArrow;//pointer or dot
}
