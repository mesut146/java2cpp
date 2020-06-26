package com.mesut.j2cpp.ast;

public class CParameter extends Node {

    public CType type;
    public CName name;


    public void setName(String name) {
        this.name = new CName(name);
    }

    public void print() {
        list.clear();
        name.isPointer = type.isPointer();
        //append(type.toString().replace(".","::"));//normalize the type(base::type)
        append(type.normal());
        append(" ");
        append(name.toString());
    }


}
