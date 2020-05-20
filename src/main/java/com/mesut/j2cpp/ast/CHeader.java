package com.mesut.j2cpp.ast;

import java.util.ArrayList;
import java.util.List;

//represents simple c++ header (.hpp)
public class CHeader extends Node {
    public String name;
    public List<String> includes = new ArrayList<>();
    public List<String> importStar = new ArrayList<>();//import namespace for symbols on the fly(java.util.*)
    public List<CClass> classes = new ArrayList<>();
    public Namespace ns;
    public List<Namespace> using = new ArrayList<>();
    public String rpath;//header path:java/lang/String.h
    boolean hasRuntime = false;

    public CHeader(String path) {
        rpath = path;
    }

    String getPathNoExt() {
        return rpath.substring(0, rpath.length() - 2);
    }

    public void addClass(CClass cc) {
        cc.ns = ns;
        classes.add(cc);
    }

    public void addRuntime() {
        hasRuntime = true;
    }

    public void addInclude(String include) {
        //prevent same header includes itself and other duplications
        if (!include.equals(getPathNoExt()) && !includes.contains(include)) {
            includes.add(include);
        }
    }

    public void addIncludeStar(String include) {
        if (!importStar.contains(include)) {
            importStar.add(include);
        }
    }

    public void useNamespace(Namespace ns) {
        if (!using.contains(ns)) {
            using.add(ns);
        }
    }

    public void useNamespace(String ns) {
        useNamespace(new Namespace(ns));
    }

    /**
     * make sure type is included
     **/
    public void validate(CType type) {
        for (String inc : includes) {
            int idx = inc.lastIndexOf("/");
            String name = inc.substring(idx + 1, inc.length() - 2);
            if (type.type.equals(name)) {
                return;
            }
        }
        //check asterisk imps
    }

    public void print() {
        append("#pragma once");
        println();
        println();
        for (String imp : includes) {
            include(imp);
        }
        if (hasRuntime) {
            include("Helper");
        }
        println();
        if (ns != null) {
            line("namespace ");
            append(ns.all);
            appendln("{");
            up();
        }
        for (Namespace use : using) {
            print_using(use);
        }

        for (CClass cc : classes) {
            cc.forHeader = true;
            append(cc);
        }
        if (ns != null) {
            down();
            lineln("}//namespace " + ns.all);
        }
    }

    public String getInclude() {
        return rpath;
    }


    public boolean isIncluded(String path) {
        for (String str : includes) {
            if (str.equals(path)) {
                return true;
            }
        }
        return false;
    }
}
