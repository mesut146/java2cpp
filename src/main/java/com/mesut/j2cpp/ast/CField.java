package com.mesut.j2cpp.ast;

public class CField extends ModifierNode {

    public CType type;
    public CName name;
    public String right;
    public CClass parent;
    public boolean ptrOnType = false;

    public void setName(String name) {
        this.name = CName.from(name);
    }

    public void print() {
        name.isPointer = type.isPointer();
        if (isStatic()) {
            append("static ");
        }

        append(type.normalize(parent.ns));
        append(" ");
        append(name.toString());
        append(";");
    }


}
