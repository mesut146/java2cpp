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

    public void setName(CName name) {
        this.name = name;
    }

    public void setType(CType type) {
        this.type = type.copy();
        this.type.setPointer(Config.ptr_parameter);
    }

    public String printFor(Object scope) {
        StringBuilder sb = new StringBuilder();
        if (scope instanceof CHeader) {
            sb.append(type.normalized(method.getHeader()));
        }
        else {
            sb.append(type.normalized(method.getHeader().source));
        }
        sb.append(" ");
        if (isVarArg) {
            sb.append("...");
        }
        sb.append(name.toString());
        return sb.toString();
    }

    public void print() {

    }


}
