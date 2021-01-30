package com.mesut.j2cpp.ast;

import com.mesut.j2cpp.Config;
import com.mesut.j2cpp.cppast.CNode;
import com.mesut.j2cpp.util.LocalForwardDeclarator;
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
    public List<Namespace> usings = new ArrayList<>();
    public String rpath;//header path:java/lang/String.h
    public CSource source;
    public LocalForwardDeclarator forwardDeclarator;
    boolean hasRuntime = false;
    int anonyCount = 0;

    public CHeader(String path) {
        rpath = path;
        scope = this;
    }

    public void setNs(Namespace ns) {
        this.ns = ns;
        useNamespace(ns);
        if (source != null) {
            source.useNamespace(ns);
        }
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
        if (!usings.contains(ns)) {
            usings.add(ns);
        }
    }

    //trim type's namespace by usings
    //java::lang::String   using java::lang -> String
    public CType normalizeType(CType type) {
        return TypeHelper.normalizeType(type, usings);
    }

    public String getAnonyName() {
        return "anony" + anonyCount++;
    }

    @Override
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
        if (forwardDeclarator != null) {
            sb.append(forwardDeclarator.toString());
            sb.append("\n");
        }

        if (ns != null && !ns.getAll().equals("")) {
            if (Config.ns_type_nested) {
                for (String cur : ns.parts) {
                    sb.append("namespace ").append(cur).append("{\n");
                    if (Config.ns_nested_indent) {
                        up();
                    }
                }
                sb.append("\n");
            }
            else {
                sb.append("namespace ").append(ns.getAll()).append("{\n");
                if (Config.ns_indent) {
                    up();
                }
            }
        }
        for (Namespace use : usings) {
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
            if (Config.ns_type_nested) {
                for (String cur : ns.parts) {
                    sb.append("}//namespace ").append(cur).append("\n");
                    if (Config.ns_nested_indent) {
                        down();
                    }
                }
            }
            else {
                sb.append("}//namespace ").append(ns.getAll());
            }
        }
        return sb.toString();
    }


    public void forward(CType type) {
        //forwardDeclarator.add(type);
    }
}
