package com.mesut.j2cpp.ast;

import com.mesut.j2cpp.Config;
import com.mesut.j2cpp.cppast.CExpression;

public class CField extends ModifierNode/*statement*/ {

    public CType type;
    public CName name;
    public CClass parent;
    public CExpression expression;

    public void setName(String name) {
        this.name = CName.from(name);
    }

    public void setName(CName name) {
        this.name = name;
    }

    public void setType(CType type) {
        this.type = type.copy();
        this.type.setPointer(Config.ptr_field);
    }

    public String forHeader() {
        StringBuilder sb = new StringBuilder();
        if (isStatic()) {
            sb.append("static ");
        }

        sb.append(type.normalized(parent.header));
        sb.append(" ");
        sb.append(name.toString());
        if (!isStatic() && expression != null) {
            sb.append(" = ");
            expression.scope = parent.header;
            sb.append(expression);
        }
        sb.append(";");
        return sb.toString();
    }


    public String forSource() {
        StringBuilder sb = new StringBuilder();
        sb.append(type.normalized(parent.header.source));
        sb.append(" ");
        sb.append(parent.name);
        sb.append("::");
        sb.append(name);
        if (expression != null) {//static
            sb.append(" = ");
            sb.append(expression.toString());
        }
        sb.append(";");
        return sb.toString();
    }


}
