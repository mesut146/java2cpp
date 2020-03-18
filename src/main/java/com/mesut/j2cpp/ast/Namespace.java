package com.mesut.j2cpp.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Namespace extends Node {
    public String all;//java:lang,org
    public List<String> parts = new ArrayList<>();//java,lang,String

    public Namespace(String ns) {
        all = ns.replace(".", "::");
        Collections.addAll(parts, all.split("::"));
    }

    public Namespace() {

    }

    public String getAll() {
        return all;
    }

    public void fromPkg(String str) {
        int i = 0;
        all = str.replace(".", "::");
        Collections.addAll(parts, str.split("::"));
    }

    public Namespace appendNs(String str) {
        if (all == null || all.length() == 0) {
            return new Namespace(str);
        }
        return new Namespace(all + "::" + str);
    }

    @Override
    public String toString() {
        return all;
    }

    @Override
    public void print() {

    }


}
