package com.mesut.j2cpp.ast;

import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

public class CField extends ModifierNode {

    public CType type;
    public CName name;
    public CClass parent;
    public boolean ptrOnType = false;
    public VariableDeclarationFragment node;

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
