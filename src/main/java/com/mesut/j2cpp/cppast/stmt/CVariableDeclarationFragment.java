package com.mesut.j2cpp.cppast.stmt;

import com.mesut.j2cpp.ast.CName;
import com.mesut.j2cpp.cppast.CExpression;
import com.mesut.j2cpp.cppast.CNode;

public class CVariableDeclarationFragment extends CNode {
    public CName name;
    public CExpression initializer;


    @Override
    public void print() {
        append(name.toString());
        if (initializer != null) {
            append(" = ");
            append(initializer);
        }
    }
}
