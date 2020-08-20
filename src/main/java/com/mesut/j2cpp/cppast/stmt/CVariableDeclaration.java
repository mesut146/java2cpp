package com.mesut.j2cpp.cppast.stmt;

import com.mesut.j2cpp.ast.CType;
import com.mesut.j2cpp.cppast.CStatement;

import java.util.ArrayList;
import java.util.List;

//type frag1,frag2;
public class CVariableDeclaration extends CStatement {
    public CType type;
    public List<CVariableDeclarationFragment> fragments = new ArrayList<>();


    @Override
    public void print() {
        append(type);
        append(" ");
        for (int i = 0; i < fragments.size(); i++) {
            append(fragments.get(i).toString());
            if (i < fragments.size() - 1) {
                append(", ");
            }
        }
        append(";");
    }
}
