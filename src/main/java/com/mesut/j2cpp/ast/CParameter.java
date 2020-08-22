package com.mesut.j2cpp.ast;

public class CParameter extends Node {

    public CType type;
    public CName name;
    public CMethodDecl method;

    public void setName(String name) {
        this.name = new CName(name);
    }

    public void print() {
        name.isPointer = type.isPointer();
        append(type.toString());
        append(" ");
        append(name.toString());
    }


}
