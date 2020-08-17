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

    public CSource(CHeader header) {
        this.header = header;
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
        for (CClass cc : header.classes) {
            printClass(cc);
        }
    }

    public void printClass(CClass cc) {
        //we directly write methods since class declarations already in CHeader
        printFields();
        printMethods();
        printInners(cc);
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

    private void printInners(CClass cc) {
        for (CClass in : cc.classes) {
            printClass(in);
        }
    }


}
