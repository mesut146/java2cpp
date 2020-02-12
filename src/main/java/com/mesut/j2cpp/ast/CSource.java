package com.mesut.j2cpp.ast;

import java.util.ArrayList;
import java.util.List;

public class CSource extends Node {
    public CHeader header;
    public List<Namespace> using = new ArrayList<>();//todo header's using instead?

    public CSource(CHeader header) {
        this.header = header;
    }

    @Override
    public void print() {
        includePath(header.getInclude());
        println();
        for (Namespace use : using) {
            print_using(use);
        }
        println();
        for (CClass cc : header.classes) {
            printClass(cc);
        }
    }

    public void printClass(CClass cc) {
        cc.forHeader = false;
        for (CMethod cm : cc.methods) {
            cm.level = 0;
            cm.init();
            append(cm);
        }
        for (CClass in : cc.classes) {
            printClass(in);
        }
    }


}
