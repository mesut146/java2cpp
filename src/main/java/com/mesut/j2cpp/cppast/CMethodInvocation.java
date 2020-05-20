package com.mesut.j2cpp.cppast;

import com.mesut.j2cpp.ast.CName;

import java.util.List;

public class CMethodInvocation extends CExpression{
    CExpression scope;
    CName name;
    List<CExpression> arguments;
    boolean isArrow;//pointer or dot
}
