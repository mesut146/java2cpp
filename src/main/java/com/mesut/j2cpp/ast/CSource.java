package com.mesut.j2cpp.ast;

import com.mesut.j2cpp.cppast.CFieldDef;

import java.util.ArrayList;
import java.util.List;

public class CSource extends Node {

    public CHeader header;
    public List<String> includes = new ArrayList<>();
    public List<Namespace> usings = new ArrayList<>();//todo header's using instead?
    public List<CFieldDef> fieldDefs = new ArrayList<>();
    public List<CMethod> methods = new ArrayList<>();
    public boolean hasRuntime = false;

    public CSource(CHeader header) {
        this.header = header;
        header.source = this;
    }

    @Override
    public void print() {
        includePath(header.getInclude());
        println();
        if (header.ns != null) {
            print_using(header.ns);
        }
        for (Namespace use : usings) {
            print_using(use);
        }
        println();
        printFields();
        printMethods();

    }

    private void printFields() {
        for (CFieldDef field : fieldDefs) {
            append(field.toString());
        }
    }

    private void printMethods() {
        for (CMethod method : methods) {
            append(method);
        }
    }


}
