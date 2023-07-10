package com.mesut.j2cpp.ast;

import com.mesut.j2cpp.Config;
import com.mesut.j2cpp.IncludeList;
import com.mesut.j2cpp.cppast.CNode;
import com.mesut.j2cpp.cppast.stmt.CBlockStatement;
import com.mesut.j2cpp.util.ForwardDeclarator;
import com.mesut.j2cpp.util.PrintHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//represents c++ header (.h)
public class CHeader extends CNode {
    public String path;//header path e.g java/lang/String.h
    public Namespace ns;
    public CClass cc;//a class per header
    public Set<Namespace> usings = new HashSet<>();
    public IncludeList includes = new IncludeList();
    public ForwardDeclarator forwardDeclarator;
    boolean handledFieldInits = false;

    public CHeader(String path) {
        this.path = path;
        scope = this;
        ns = new Namespace();
    }

    public void setNs(Namespace ns) {
        this.ns = ns;
        usings.add(ns);
    }

    public String getInclude() {
        return path;
    }

    public void setClass(CClass cc) {
        cc.ns = ns;
        cc.header = this;
        this.cc = cc;
    }

    private void handleFieldInits() {
        if (handledFieldInits || cc == null) return;
        //handle field initializers

        if (!cc.consStatements.isEmpty()) {
            List<CMethod> consList = new ArrayList<>();
            //append all cons
            for (CMethod method : cc.methods) {
                //filter
                if (method.isCons && method.thisCall == null) {
                    consList.add(method);
                }
            }
            if (consList.isEmpty()) {
                //make default constructor
                CMethod cons = new CMethod();
                cons.isCons = true;
                cons.name = CName.from(cc.name);
                cons.setPublic(true);
                cons.body = new CBlockStatement();
                consList.add(cons);
                cc.addMethod(cons);
            }
            for (CMethod cons : consList) {
                //eclipse creates default ctor so initialize
                if (cons.body == null) cons.body = new CBlockStatement();
                cons.body.statements.addAll(0, cc.consStatements);
            }
        }
        handledFieldInits = true;
    }

    @Override
    public String toString() {
        handleFieldInits();
        StringBuilder sb = new StringBuilder();
        sb.append("#pragma once\n\n");
        sb.append(includes);
        sb.append("\n");
        if (forwardDeclarator != null) {
            sb.append(forwardDeclarator);
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
        if (cc != null) {
            getScope(cc);
            sb.append(PrintHelper.body(cc.toString(), getIndent()));
        }

        sb.append("\n");

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
}
