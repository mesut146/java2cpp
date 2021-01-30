package com.mesut.j2cpp.util;

import com.mesut.j2cpp.ClassMap;
import com.mesut.j2cpp.ast.CClass;
import com.mesut.j2cpp.ast.Namespace;
import com.mesut.j2cpp.ast.Node;
import com.mesut.j2cpp.ast.Template;
import com.mesut.j2cpp.cppast.CNode;
import com.mesut.j2cpp.util.PrintHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//collect all referenced classes in a header file
public class LocalForwardDeclarator extends Node {
    public List<ClassMap.ClassDecl> types = new ArrayList<>();
    List<NamespaceDecl> baseList = new ArrayList<>();
    ClassMap classMap;

    public LocalForwardDeclarator(ClassMap classMap) {
        this.classMap = classMap;
    }

    public void add(ClassMap.ClassDecl type, Namespace namespace) {
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
        NamespaceDecl cur = getNsDecl(namespace.parts.get(0), baseList);
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
        for (NamespaceDecl dec : baseList) {
            sb.append(dec);
        }
        return sb.toString();
    }

    //single ns decl,can hold classses and other ns decl
    static class NamespaceDecl extends Node {
        public List<ClassMap.ClassDecl> classList = new ArrayList<>();
        public List<NamespaceDecl> nsList = new ArrayList<>();
        String ns;

        public NamespaceDecl(String ns) {
            this.ns = ns;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("namespace ");
            sb.append(ns);
            sb.append("{\n");

            for (ClassMap.ClassDecl dec : classList) {
                sb.append(PrintHelper.body(dec.toString(), "    "));
            }
            if (!classList.isEmpty()) {
                sb.append("\n");
            }
            for (NamespaceDecl node : nsList) {
                sb.append(PrintHelper.body(node.toString(), "    ")).append("\n");
            }

            sb.append("}").append("//namespace ").append(ns);
            return sb.toString();
        }
    }

}
