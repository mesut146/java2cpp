package com.mesut.j2cpp.ast;

import com.mesut.j2cpp.cppast.CClassImpl;
import com.mesut.j2cpp.cppast.CNode;
import com.mesut.j2cpp.util.PrintHelper;
import com.mesut.j2cpp.util.TypeHelper;

import java.util.ArrayList;
import java.util.List;

public class CSource extends CNode {

    public CHeader header;
    public List<String> includes = new ArrayList<>();
    public List<Namespace> usings = new ArrayList<>();
    public List<CField> fieldDefs = new ArrayList<>();
    public List<CMethod> methods = new ArrayList<>();
    public boolean hasRuntime = false;
    public List<CClassImpl> anony = new ArrayList<>();

    public CSource(CHeader header) {
        this.header = header;
        header.source = this;
        scope = this;
    }

    public void addInclude(String include) {
        //prevent same header includes itself and other duplications
        if (!include.equals(header.getPathNoExt()) && !includes.contains(include)) {
            includes.add(include);
        }
    }

    public void addInclude(CType include) {
        addInclude(include.basicForm().replace("::", "/") + ".h");
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String inc : includes) {
            sb.append(PrintHelper.include(inc)).append("\n");
        }
        //sb.append(PrintHelper.include(header.getInclude()));
        sb.append("\n\n");
        if (!usings.isEmpty()) {
            for (Namespace use : usings) {
                sb.append(PrintHelper.using(use)).append("\n");
            }
        }
        sb.append("\n");
        printAnony(sb);
        printFields(sb);
        printMethods(sb);
        return sb.toString();
    }

    void printAnony(StringBuilder sb) {
        if (!anony.isEmpty()) {
            getScope(anony);
            sb.append("//anonymous classes\n");
            for (CClassImpl impl : anony) {
                sb.append(PrintHelper.body(impl.toString(), "")).append("\n");
            }
            sb.append("\n");
        }
    }

    private void printFields(StringBuilder sb) {
        //todo separate by class
        if (!fieldDefs.isEmpty()) {
            getScope(fieldDefs);
            sb.append("//static fields\n");
            for (CField field : fieldDefs) {
                if (field.isStatic() && field.expression != null) {
                    sb.append(field).append("\n");
                }
            }
            sb.append("\n");
        }
    }

    private void printMethods(StringBuilder sb) {
        if (!methods.isEmpty()) {
            sb.append("//methods\n");
            getScope(methods);
            for (CMethod method : methods) {
                sb.append(PrintHelper.body(method.toString(), "")).append("\n");
            }
            sb.append("\n");
        }
    }

}
