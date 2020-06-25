package com.mesut.j2cpp.ast;

public class CParameter extends Node {

    public CType type;
    public CName name;
    //public boolean isPointer=true;


    public void setName(String name) {
        this.name = new CName(name);
    }

    public void print() {
        list.clear();
        if (type == null) {
            System.out.println("name=" + name);
        }
        //append(type.toString().replace(".","::"));//normalize the type(base::type)
        append(type.toString());
        /*if (type.isPointer()) {
            append("*");
        }*/
        append(" ");
        append(name.toString());
    }


}
