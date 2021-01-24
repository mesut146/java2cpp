package com.mesut.j2cpp.ast;

import com.mesut.j2cpp.Config;
import com.mesut.j2cpp.cppast.CExpression;

public class CField extends ModifierNode/*statement*/ {

    public CType type;
    public CName name;
    public CClass parent;
    public CExpression expression;

    public CField() {
    }

    public CField(CType type, CName name) {
        this.type = type;
        this.name = name;
    }

    public CField setName(CName name) {
        this.name = name;
        return this;
    }

    public CField setType(CType type) {
        this.type = type;
        return this;
    }

    @Override
    public String toString() {
        type = type.copy();
        this.type.setPointer(Config.ptr_field);
        if (scope instanceof CHeader) {
            return forHeader();
        }
        return forSource();
    }

    public String forHeader() {
        getScope(type, name, expression);
        StringBuilder sb = new StringBuilder();
        if (isStatic()) {
            sb.append("static ");
        }
        sb.append(type);
        sb.append(" ");
        sb.append(name);
        if (!isStatic() && expression != null) {
            sb.append(" = ");
            sb.append(expression);
        }
        sb.append(";");
        return sb.toString();
    }

    public String forSource() {
        getScope(type, name, expression);
        StringBuilder sb = new StringBuilder();
        sb.append(type);
        sb.append(" ");
        if (!parent.isAnonymous) {
            sb.append(parent.name);
            sb.append("::");
        }
        sb.append(name);
        if (expression != null) {//static
            sb.append(" = ");
            sb.append(expression);
        }
        sb.append(";");
        return sb.toString();
    }


}
