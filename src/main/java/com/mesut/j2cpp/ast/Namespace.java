package com.mesut.j2cpp.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Namespace extends Node {
    public String all;//java:lang,org
    public List<String> parts = new ArrayList<>();//java,lang,String

    public Namespace(String ns) {
        all = ns.replace(".", "::");
        Collections.addAll(parts, all.split("::"));
    }

    public Namespace(List<String> ns) {
        parts = ns;
        all = String.join("::", parts);
    }

    public Namespace() {
        this("");
    }

    public String getAll() {
        return all;
    }

    @Override
    public String toString() {
        return getAll();
    }

    public boolean isEmpty(){
        return parts.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Namespace namespace = (Namespace) o;
        return Objects.equals(parts, namespace.parts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parts);
    }
}
