package com.mesut.j2cpp;

import com.github.javaparser.ast.CompilationUnit;

//maps class name to unit
class UnitMap {
    CompilationUnit cu;
    String name;//asd.java

    public UnitMap(CompilationUnit cu, String name) {
        this.cu = cu;
        this.name = name;
    }
}
