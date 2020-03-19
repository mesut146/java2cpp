package com.mesut.j2cpp.ast;

public class CField extends ModifierNode {

    public CType type;
    public String name;
    public String right;


    public void print() {
        if (isStatic()) {
            append("static ");
        }
        append(type.toString());
        append(" ");
        append(name);
        if (right != null) {
            append(" = ");
            append(right);
        }
        append(";");
    }


}
