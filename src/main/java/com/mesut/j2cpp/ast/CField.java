package com.mesut.j2cpp.ast;

public class CField extends ModifierNode {
    public CType type;
    public String name, right;


    public void print() {
        if (isStatic()) {
            append("static ");
        }
        append(type.toString());
        /*if (type.isPointer()) {
            append("*");
        }*/
        append(" ");
        append(name);
        if (right != null) {
            append(" = ");
            append(right);
        }
        append(";");
        //TODO right
    }


}
