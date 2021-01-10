package com.mesut.j2cpp.ast;

import com.mesut.j2cpp.cppast.CNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//collect all referenced classes in a header file
public class LocalForwardDeclarator extends CNode {
    public List<CType> list = new ArrayList<>();
    public CHeader header;
    List<NamespaceDecl> baseList = new ArrayList<>();
    Map<String, NamespaceDecl> map = new HashMap<>();

    public LocalForwardDeclarator(CHeader header) {
        this.header = header;
    }

    public void add(CType type) {
        if (!list.contains(type)) {
            list.add(type);
        }
    }

    NamespaceDecl getDecl(Namespace ns) {
        if (map.containsKey(ns.getAll())) {
            return map.get(ns.getAll());
        }
        NamespaceDecl decl = new NamespaceDecl(ns.parts.get(ns.parts.size() - 1));
        map.put(ns.getAll(), decl);
        if (ns.parts.size() > 1) {
            Namespace par = new Namespace(ns.parts.subList(0, ns.parts.size() - 1));
            NamespaceDecl parent = getDecl(par);
            parent.list.add(decl);
        }
        else {
            //i am parent of all
            //add to list
            baseList.add(decl);
        }
        return decl;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("//forward declarations\n");
        //group ny namespace
        for (CType type : list) {
            NamespaceDecl decl = getDecl(type.ns);
            decl.decls.add(new ClassDecl(type.type));
        }
        //print
        for (NamespaceDecl dec : baseList) {
            append(dec);
        }
        return sb.toString();
    }

    static class ClassDecl extends CNode {
        String name;
        Template template;

        public ClassDecl(String name) {
            this.name = name;
        }

        @Override
        public void print() {
            append("class ");
            append(name);
            append(";");
        }
    }

    static class NamespaceDecl extends CNode {
        String ns;
        public List<NamespaceDecl> list = new ArrayList<>();
        public List<ClassDecl> decls = new ArrayList<>();

        public NamespaceDecl(String ns) {
            this.ns = ns;
        }

        @Override
        public void print() {
            append("namespace ");
            append(ns);
            append("{");
            up();

            for (NamespaceDecl node : list) {
                appendIndent(node);
            }
            for (ClassDecl dec : decls) {
                appendIndent(dec);
            }

            down();
            line("}");
            append("//namespace " + ns);
        }
    }

}
