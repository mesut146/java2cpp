package com.mesut.j2cpp.ast;

import com.mesut.j2cpp.cppast.CClassImpl;
import com.mesut.j2cpp.cppast.CNode;
import com.mesut.j2cpp.cppast.CStatement;
import com.mesut.j2cpp.cppast.expr.CMethodInvocation;
import com.mesut.j2cpp.cppast.stmt.CBlockStatement;
import com.mesut.j2cpp.util.PrintHelper;
import com.mesut.j2cpp.util.TypeHelper;

import java.util.ArrayList;
import java.util.List;

public class CSource extends CNode {

    public CHeader header;
    public List<String> includes = new ArrayList<>();
    public List<Namespace> usings = new ArrayList<>();
    public List<CField> fieldDefs = new ArrayList<>();
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
        //can be local class,those already included by converter so ignore
        for (CClass cc : header.classes) {
            if (cc.getType().equals(include)) {
                return;
            }
        }
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
        //todo group by class(or they already ordered)
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
        for (CClass cc : header.classes) {
            printMethods(sb, cc);
        }
    }

    void printMethods(StringBuilder sb, CClass cc) {
        List<CMethod> methods = cc.methods;
        if (!methods.isEmpty()) {
            sb.append("//methods for ").append(cc.name).append("\n");
            getScope(methods);
            for (CMethod method : methods) {
                if (method.body == null) continue;
                sb.append(PrintHelper.body(method.toString(), "")).append("\n");
            }
            for (CClass inner : cc.classes) {
                printMethods(sb, inner);
            }
            sb.append("\n");
        }
    }

}
