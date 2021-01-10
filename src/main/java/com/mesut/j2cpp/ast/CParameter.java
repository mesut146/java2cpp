package com.mesut.j2cpp.ast;

import com.mesut.j2cpp.Config;
import com.mesut.j2cpp.cppast.CNode;

public class CParameter extends CNode {

    public CType type;
    public CName name;
    public CMethod method;
    public boolean isVarArg = false;

    public void setName(String name) {
        this.name = new CName(name);
    }

    public void setName(CName name) {
        this.name = name;
    }

    public void setType(CType type) {
        this.type = type.copy();
        this.type.setPointer(Config.ptr_parameter);
    }

    public String toString() {
        getScope(type);
        StringBuilder sb = new StringBuilder();
        sb.append(type);
        sb.append(" ");
        if (isVarArg) {
            sb.append("...");
        }
        sb.append(name.toString());
        return sb.toString();
    }



}
