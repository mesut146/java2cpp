package com.mesut.j2cpp.cppast.expr;

import com.mesut.j2cpp.ast.CName;
import com.mesut.j2cpp.cppast.CExpression;

import java.util.List;

public class CMethodInvocation extends CExpression {
    CExpression scope;
    CName name;
    List<CExpression> arguments;
    boolean isArrow;//pointer or dot
}
