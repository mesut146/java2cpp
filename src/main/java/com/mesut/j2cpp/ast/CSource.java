package com.mesut.j2cpp.ast;

import java.util.ArrayList;
import java.util.List;

public class CSource extends Node {

    public CHeader header;
    public List<Namespace> usings = new ArrayList<>();//todo header's using instead?

    public CSource(CHeader header) {
        this.header = header;
    }

    @Override
    public void print() {
        includePath(header.getInclude());
        println();
        if (header.ns != null) {
            append(header.ns);
            up();
        }
        for (Namespace use : usings) {
            print_using(use);
        }
        println();
        for (CClass cc : header.classes) {
            printClass(cc);
        }
        if (header.ns != null) {
            down();
        }
    }

    public void printClass(CClass cc) {
        cc.forHeader = false;
        //we directly write methods since class declarations already in CHeader
        //write methods
        for (CMethod cm : cc.methods) {
            cm.level = 0;
            cm.init();
            append(cm);
        }
        //write inner classes
        for (CClass in : cc.classes) {
            printClass(in);
        }
    }


}
