package com.mesut.j2cpp.ast;

import com.mesut.j2cpp.Config;

public class CParameter extends Node {

    public CType type;
    public CName name;
    public CMethod method;
    public boolean isVarArg = false;

    public void setName(String name) {
        this.name = new CName(name);
    }

    public void setType(CType type) {
        this.type = type.copy();
        this.type.setPointer(Config.ptr_parameter);
    }

    public void print() {
        append(type.toString());
        append(" ");
        if (isVarArg) {
            append("...");
        }
        append(name.toString());
    }


}
