package com.mesut.j2cpp.ast;

import com.mesut.j2cpp.util.TypeHelper;

import java.util.ArrayList;
import java.util.List;

//represents simple c++ header (.hpp)
public class CHeader extends Node {
    public String name;
    public List<String> includes = new ArrayList<>();
    public List<CClass> classes = new ArrayList<>();
    public Namespace ns;
    public List<Namespace> using = new ArrayList<>();
    public String rpath;//header path:java/lang/String.h
    boolean hasRuntime = false;
    public CSource source;
    //public LocalForwardDeclarator forwardDeclarator = new LocalForwardDeclarator(this);

    public CHeader(String path) {
        rpath = path;
        scope = this;
    }

    String getPathNoExt() {
        return rpath.substring(0, rpath.length() - 2);
    }

    public void addClass(CClass cc) {
        cc.ns = ns;
        cc.header = this;
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


    public void useNamespace(Namespace ns) {
        if (!using.contains(ns)) {
            using.add(ns);
        }
    }

    public void useNamespace(String ns) {
        useNamespace(new Namespace(ns));
    }


    //trim type's namespace by usings
    //java::lang::String   using java::lang -> String
    public CType normalizeType(CType type) {
        return TypeHelper.normalizeType(type, using);
    }

    int anonyCount = 0;

    public String getAnonyName() {
        return "anony" + anonyCount++;
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

        //append(forwardDeclarator);
        println();

        if (ns != null && !ns.all.equals("")) {
            line("namespace ");
            append(ns.all);
            appendln("{");
            up();
        }
        for (Namespace use : using) {
            if (ns == null || !use.all.equals(ns.all)) {//omit current namespace
                print_using(use);
            }
        }

        for (CClass cc : classes) {
            append(cc);
        }
        if (ns != null && !ns.all.equals("")) {
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

    public void forward(CType type) {
        //forwardDeclarator.add(type);
    }
}
