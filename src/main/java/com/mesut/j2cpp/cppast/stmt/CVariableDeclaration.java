package com.mesut.j2cpp.cppast.stmt;

import com.mesut.j2cpp.ast.CType;
import com.mesut.j2cpp.cppast.CStatement;

import java.util.List;

//type frag1,frag2;
public class CVariableDeclaration extends CStatement {
    CType type;
    List<CVariableDeclarationFragment> fragments;
}
