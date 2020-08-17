package com.mesut.j2cpp.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Namespace extends Node {
    public String all;//java:lang,org
    public List<String> parts = new ArrayList<>();//java,lang,String

    public Namespace(String ns) {
        fromPkg(ns);
    }

    public Namespace(List<String> ns) {
        parts = ns;
        all = String.join("::", parts);
    }

    public Namespace() {

    }

    public void fromPkg(String str) {
        all = str.replace(".", "::");
        Collections.addAll(parts, all.split("::"));
    }

    //trim common namespace
    public String normalize(Namespace scope) {
        int i = 0;
        int len = Math.min(parts.size(), scope.parts.size());
        List<String> list = new ArrayList<>(parts);
        while (i < len) {
            if (parts.get(i).equals(scope.parts.get(i))) {
                i++;
            }
            else {
                break;
            }
        }
        return new Namespace(list.subList(i, list.size())).all;
    }

    public String getAll() {
        return all;
    }


    @Override
    public String toString() {
        return all;
    }

    @Override
    public void print() {

    }


}
