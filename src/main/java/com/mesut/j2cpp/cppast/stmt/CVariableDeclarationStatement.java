package com.mesut.j2cpp.cppast.stmt;

import com.mesut.j2cpp.Config;
import com.mesut.j2cpp.ast.CType;
import com.mesut.j2cpp.cppast.CStatement;
import com.mesut.j2cpp.util.PrintHelper;

import java.util.ArrayList;
import java.util.List;

//type frag1,frag2;
public class CVariableDeclarationStatement extends CStatement {
    public CType type;
    public List<CVariableDeclarationFragment> fragments = new ArrayList<>();

    public void setType(CType type) {
        this.type = type.copy();
        this.type.setPointer(Config.ptr_varDecl);
    }

    @Override
    public String toString() {
        getScope(type);
        getScope(fragments);
        return type + " " + PrintHelper.joinStr(fragments, ", ") + ";";
    }
}
