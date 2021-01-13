package com.mesut.j2cpp.ast;

import com.mesut.j2cpp.cppast.CNode;
import com.mesut.j2cpp.util.PrintHelper;
import com.mesut.j2cpp.util.TypeHelper;

import java.util.ArrayList;
import java.util.List;

//represents c++ header (.hpp)
public class CHeader extends CNode {
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

    public String getInclude() {
        return rpath;
    }

    String getPathNoExt() {
        return rpath.substring(0, rpath.length() - 2);
    }

    public void addClass(CClass... arr) {
        for (CClass cc : arr) {
            cc.ns = ns;
            cc.header = this;
            classes.add(cc);
        }
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

    //trim type's namespace by usings
    //java::lang::String   using java::lang -> String
    public CType normalizeType(CType type) {
        return TypeHelper.normalizeType(type, using);
    }

    int anonyCount = 0;

    public String getAnonyName() {
        return "anony" + anonyCount++;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("#pragma once\n\n");
        for (String imp : includes) {
            sb.append(String.format("#include \"%s.h\"", imp));
            sb.append("\n");
        }
        if (hasRuntime) {
            sb.append(String.format("#include \"%s.h\"", "Helper"));
            sb.append("\n");
        }
        sb.append("\n");
        //append(forwardDeclarator);
        sb.append("\n");

        if (ns != null && !ns.all.equals("")) {
            sb.append("namespace ").append(ns.getAll()).append("{\n");
            up();
        }
        for (Namespace use : using) {
            if (ns == null || !use.getAll().equals(ns.getAll())) {//omit current namespace
                sb.append(getIndent()).append(PrintHelper.using(use));
                sb.append("\n");
            }
        }
        getScope(classes);
        for (CClass cc : classes) {
            sb.append(PrintHelper.body(cc.toString(), getIndent()));
            sb.append("\n");
        }
        if (ns != null && !ns.getAll().equals("")) {
            sb.append("}//namespace ").append(ns.getAll());
        }
        return sb.toString();
    }


    public void forward(CType type) {
        //forwardDeclarator.add(type);
    }
}
