package com.mesut.j2cpp.ast;

public class CField extends ModifierNode {

    public CType type;
    public CName name;
    public String right;

    public void setName(String name) {
        this.name = CName.from(name);
    }

    public void print() {
        name.isPointer = type.isPointer();
        if (isStatic()) {
            append("static ");
        }

        append(type.normal());
        append(" ");
        append(name.toString());
        if (right != null && !isStatic()) {
            append(" = ");
            append(right);
        }
        append(";");
    }


}
