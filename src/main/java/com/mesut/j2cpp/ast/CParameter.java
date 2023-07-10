package com.mesut.j2cpp.ast;

import com.mesut.j2cpp.Config;
import com.mesut.j2cpp.cppast.CNode;

public class CParameter extends CNode {

    public CType type;
    public CName name;
    public boolean isVarArg = false;

    public CParameter(CType type, CName name) {
        this.type = type;
        this.name = name;
    }

    public CParameter() {
    }

    public void setName(CName name) {
        this.name = name;
    }

    public void setType(CType type) {
        this.type = type;
    }

    public String toString() {
        this.type = type.copy();
        this.type.setPointer(Config.ptr_parameter);
        getScope(type, name);
        StringBuilder sb = new StringBuilder();
        sb.append(type);
        sb.append(" ");
        if (isVarArg) {
            sb.append("...");
        }
        if (Config.printParamNames || scope instanceof CSource) {
            sb.append(name);
        }
        return sb.toString();
    }


}
