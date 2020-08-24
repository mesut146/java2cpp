package com.mesut.j2cpp.ast;

import com.mesut.j2cpp.cppast.CExpression;

public class CField extends ModifierNode/*statement*/ {

    public CType type;
    public CName name;
    public CClass parent;
    public boolean ptrOnType = false;
    public CExpression expression;

    public void setName(String name) {
        this.name = CName.from(name);
    }

    //for header
    public void print() {
        if (isStatic()) {
            append("static ");
        }

        append(type.toString());
        append(" ");
        append(name.toString());
        if (!isStatic() && expression != null) {
            append(" = ");
            append(expression.toString());
        }
        append(";");
    }

    public void printAll(boolean source) {
        if (!source && isStatic()) {
            append("static ");
        }

        append(type.toString());
        append(" ");
        if (source) {
            append(parent.name);
        }
        append(name.toString());

        if (expression != null) {
            if (isStatic() && source || !isStatic() && !source) {
                append(" = ");
                append(expression.toString());
            }
        }
        append(";");
    }

    public String forSource() {
        StringBuilder sb = new StringBuilder();
        sb.append(type.toString());
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
