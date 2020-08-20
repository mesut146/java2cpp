package com.mesut.j2cpp.ast;

import com.mesut.j2cpp.cppast.CClassImpl;
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
    public List<CClassImpl> anony = new ArrayList<>();

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
        printAnony();
        line("");
        printFields();
        line("");
        printMethods();

    }

    void printAnony() {
        if (!anony.isEmpty()) {
            line("//anonymous classes");
            for (CClassImpl impl : anony) {
                append(impl);
            }
        }
    }

    private void printFields() {
        for (CFieldDef field : fieldDefs) {
            if (field.hasExpression()) {
                line(field.toString());
            }
        }
    }

    private void printMethods() {
        for (CMethod method : methods) {
            append(method);
        }
    }


}
