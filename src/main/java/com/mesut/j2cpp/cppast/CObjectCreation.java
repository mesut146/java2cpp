package com.mesut.j2cpp.cppast;

import com.mesut.j2cpp.ast.CType;

import java.util.List;

//new ns::Type(args)
public class CObjectCreation extends CExpression{
    CType type;
    List<Object> args;
}
