package com.mesut.j2cpp.ast;

import com.mesut.j2cpp.Config;
import com.mesut.j2cpp.IncludeList;
import com.mesut.j2cpp.cppast.CClassImpl;
import com.mesut.j2cpp.util.PrintHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CSource extends Node {

    public List<CClass> classes = new ArrayList<>();
    public String name;
    public IncludeList includes = new IncludeList();
    public Set<Namespace> usings = new HashSet<>();
    public List<CField> fieldDefs = new ArrayList<>();
    public boolean hasRuntime = false;
    public List<CClassImpl> anony = new ArrayList<>();

    public CSource() {
        scope = this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(includes);
        sb.append("\n\n");
        for (Namespace use : usings) {
            if (use.parts.isEmpty()) continue;
            sb.append(PrintHelper.using(use)).append("\n");
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
        //todo group by class(or they already ordered?)
        if (!fieldDefs.isEmpty()) {
            getScope(fieldDefs);
            sb.append("//static fields\n");
            for (CField field : fieldDefs) {
                if (!field.isStatic() || field.expression == null) {
                    continue;
                }
                if (Config.static_field_cofui) {
                    //create method
                    sb.append(field.type).append("& ");
                    sb.append(field.parent.type);
                    sb.append("::");
                    sb.append(field.name).append("(){\n");
                    sb.append("    auto static tmp = ").append(field.expression).append(";\n");
                    sb.append("    return tmp;\n");
                    sb.append("}\n");
                }
                else {
                    sb.append(field).append("\n");
                }
            }
            sb.append("\n");
        }
    }

    private void printMethods(StringBuilder sb) {
        for (CClass cc : classes) {
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
            sb.append("\n");
        }
    }


}
