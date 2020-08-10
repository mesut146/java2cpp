package com.mesut.j2cpp.cppast.expr;

import com.mesut.j2cpp.ast.CType;
import com.mesut.j2cpp.cppast.CExpression;

import java.util.List;

//new ns::Type(args)
public class CObjectCreation extends CExpression {
    CType type;
    List<Object> args;
}
