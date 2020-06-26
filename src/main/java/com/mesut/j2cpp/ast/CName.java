package com.mesut.j2cpp.ast;

//a common name
//var name,method name,param name,class name
public class CName {

    public Namespace namespace;//for class names
    public String name;
    public boolean isPointer = false;

    public CName(String name) {
        this.name = name;
    }

    public static CName from(String name){
        return new CName(name);
    }



    @Override
    public String toString() {
        if (isPointer) {
            return "*" + name;
        }
        return name;
    }
}
