package com.mesut.j2cpp.cppast;

import com.mesut.j2cpp.ast.CClass;

//class with dec and def together
public class CClassDeclStatement extends CStatement {
    CClass clazz;

    public CClassDeclStatement(CClass clazz) {
        this.clazz = clazz;
    }


    @Override
    public void print() {

    }
}
