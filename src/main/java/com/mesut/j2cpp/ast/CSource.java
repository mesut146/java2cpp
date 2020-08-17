package com.mesut.j2cpp.ast;

import java.util.ArrayList;
import java.util.List;

public class CSource extends Node {

    public CHeader header;
    public List<String> includes = new ArrayList<>();
    public List<Namespace> usings = new ArrayList<>();//todo header's using instead?
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
        printFields(cc);
        printMethods(cc);
        printInners(cc);
    }

    private void printFields(CClass cc) {
        for (CField field : cc.fields) {
            if (field.right != null && field.isStatic()) {
                //clazz::name=val;
                append(field.type);
                append(" ");
                append(cc.name);
                append("::");
                append(field.name.name);
                append(" = ");
                append(field.right);
                appendln(";");
            }
        }
    }

    private void printMethods(CClass cc) {
        /*for (CMethod cm : cc.methods) {
            cm.level = 0;
            cm.init();
            append(cm);
        }*/
    }

    private void printInners(CClass cc) {
        for (CClass in : cc.classes) {
            printClass(in);
        }
    }


}
