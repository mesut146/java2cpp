package com.mesut.j2cpp.cppast.stmt;

import com.mesut.j2cpp.Config;
import com.mesut.j2cpp.ast.CType;
import com.mesut.j2cpp.cppast.CStatement;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

//type frag1,frag2;
public class CVariableDeclarationStatement extends CStatement {
    public CType type;
    public List<CVariableDeclarationFragment> fragments = new ArrayList<>();

    public void setType(CType type) {
        this.type = type.copy();
        this.type.setPointer(Config.ptr_varDecl);
    }

    @Override
    public void print() {
        append(type);
        append(" ");
        int i = 0;
        for (CVariableDeclarationFragment fragment : fragments) {
            append(fragment);
            if (i < fragments.size() - 1) append(", ");
            i++;
        }
        append(";");
    }
}
