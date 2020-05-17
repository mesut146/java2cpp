package com.mesut.j2cpp.ast;

//a common name
//var name,method name,param name,class name
public class CName {

    public Namespace namespace;//for class names
    public String name;
    public boolean isPointer = false;


    @Override
    public String toString() {
        if (isPointer) {
            return "*" + name;
        }
        return name;
    }
}
