package com.mesut.j2cpp;

import java.util.ArrayList;
import java.util.List;

public class SymbolTable {

    List<Symbol> list = new ArrayList<>();

    public void addSymbol(String pkg, String name) {
        pkg=pkg.replace(".","::");
        list.add(new Symbol(pkg, name));
    }
}

//represents source file in pgk and name
class Symbol {
    String pkg;
    String name;

    public Symbol(String pkg, String name) {
        this.pkg = pkg;
        this.name = name;
    }
}
