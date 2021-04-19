package com.mesut.j2cpp.util;

import com.mesut.j2cpp.ast.CClass;
import com.mesut.j2cpp.ast.Namespace;
import com.mesut.j2cpp.ast.Node;
import com.mesut.j2cpp.map.ClassMap;

import java.util.ArrayList;
import java.util.List;

public class ForwardDeclarator {
    public List<CClass> types = new ArrayList<>();
    List<NamespaceDecl> nsList = new ArrayList<>();
    NamespaceDecl nons = new NamespaceDecl("");
    ClassMap classMap;

    public ForwardDeclarator(ClassMap classMap) {
        this.classMap = classMap;
    }

    public void add(CClass type, Namespace namespace) {
        if (!types.contains(type)) {
            NamespaceDecl ns = getNsDecl(namespace);
            types.add(type);
            ns.classList.add(type);
        }
    }

    public void addAll(List<CClass> classes) {
        for (CClass cc : classes) {
            add(cc);
        }
    }

    public void add(CClass cc) {
        add(classMap.get(cc.getType()), cc.ns);
    }

    //create or get ns block
    NamespaceDecl getNsDecl(Namespace namespace) {
        if (namespace.parts.isEmpty()) return nons;
        NamespaceDecl cur = getNsDecl(namespace.parts.get(0), nsList);
        for (int i = 1; i < namespace.parts.size(); i++) {
            cur = getNsDecl(namespace.parts.get(i), cur.nsList);
        }
        return cur;
    }

    //get or create ns in decl
    NamespaceDecl getNsDecl(String ns, List<NamespaceDecl> list) {
        for (NamespaceDecl cand : list) {
            if (cand.ns.equals(ns)) {
                return cand;
            }
        }
        NamespaceDecl decl = new NamespaceDecl(ns);
        list.add(decl);
        return decl;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("//forward declarations\n");
        for (NamespaceDecl dec : nsList) {
            sb.append(dec);
            sb.append("\n");
        }
        return sb.toString();
    }

    //single ns decl,can hold classses and other ns decl
    static class NamespaceDecl extends Node {
        public List<CClass> classList = new ArrayList<>();
        public List<NamespaceDecl> nsList = new ArrayList<>();
        String ns;

        public NamespaceDecl(String ns) {
            this.ns = ns;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            String indent = "";
            if (!ns.isEmpty()) {
                sb.append("namespace ");
                sb.append(ns);
                sb.append("{\n");
                indent = "    ";
            }

            for (CClass dec : classList) {
                sb.append(PrintHelper.body(dec.forwardStr(), indent));
            }
            if (!classList.isEmpty()) {
                sb.append("\n");
            }
            for (NamespaceDecl node : nsList) {
                sb.append(PrintHelper.body(node.toString(), "    ")).append("\n");
            }
            if (!ns.isEmpty()) {
                sb.append("}").append("//namespace ").append(ns);
            }
            return sb.toString();
        }
    }

}
