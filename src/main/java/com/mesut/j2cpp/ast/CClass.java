package com.mesut.j2cpp.ast;

import com.mesut.j2cpp.Config;
import com.mesut.j2cpp.Writer;
import com.mesut.j2cpp.cppast.CNode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CClass extends CNode {

    public String name;
    public Namespace ns = null;
    public List<CType> base = new ArrayList<>();
    public Template template = new Template();
    public List<CField> fields = new ArrayList<>();
    public List<CMethodDecl> methods = new ArrayList<>();
    public List<CClass> classes = new ArrayList<>();
    public boolean isInterface = false;
    public boolean isEnum = false;//todo
    public CClass parent;//outer
    public CHeader header;
    public Writer staticBlock = null;

    public CClass() {
        if (Config.baseClassObject) {
            base.add(new CType("java::lang::Object"));
        }
    }

    public void addInner(CClass cc) {
        cc.parent = this;
        cc.header = header;
        classes.add(cc);
    }

    public void addMethod(CMethodDecl cm) {
        cm.parent = this;
        methods.add(cm);
    }

    public void addField(CField cf) {
        cf.parent = this;
        fields.add(cf);
    }

    public Template getTemplate() {
        return template;
    }

    public Namespace getNamespace() {
        String str;
        if (parent == null) {//header level
            return ns;
        }
        str = parent.getNamespace().all + "::" + name;
        return new Namespace(str);
    }

    public Namespace getNamespaceFull() {
        String str;
        Namespace n = new Namespace();
        if (parent != null) {
            str = parent.getNamespace().all + "::";
            n.fromPkg(str);
        }
        else if (ns != null) {
            n.fromPkg(ns.all + "::" + name);
        }
        return n;
    }

    public void print() {
        printDecl();
        append("{");
        up();
        //impl
        if (staticBlock != null) {
            println();
            println();
            appendIndent(staticBlock);
            println();
        }
        line("//fields");
        printFields();
        line("//methods");
        printMethods();
        //inner classes
        for (CClass cc : classes) {
            setTo(cc);
            append(cc);
        }
        down();
        lineln("};//class " + name);
    }

    private void printDecl() {
        if (!template.isEmpty()) {
            println();
            append(template.toString());
        }
        if (isInterface) {
            line("/*interface*/");
        }
        //class decl
        line("class ");
        append(name);
        if (base.size() > 0) {
            append(": public ");
            for (int i = 0; i < base.size(); i++) {
                append(base.get(i).normal());
                if (i < base.size() - 1) {
                    append(",");
                }
            }
        }
    }

    private void printMethods(List<CMethodDecl> list, String modifier) {
        if (list.size() > 0) {
            line(modifier);
            up();
            for (CMethodDecl cm : list) {
                setTo(cm);
                append(cm);
            }
            down();
        }
    }

    private void printMethods() {
        List<CMethodDecl> public_methods = methods.stream().filter(CMethodDecl::isPublic).collect(Collectors.toList());
        List<CMethodDecl> priv_methods = methods.stream().filter(CMethodDecl::isPrivate).collect(Collectors.toList());
        printMethods(public_methods, "public:");
        printMethods(priv_methods, "private:");
        println();
    }

    private void printFields() {
        List<CField> public_fields = fields.stream().filter(CField::isPublic).collect(Collectors.toList());
        List<CField> priv_fields = fields.stream().filter(CField::isPrivate).collect(Collectors.toList());
        printFields(public_fields, "public:");
        printFields(priv_fields, "private:");
        println();
    }

    private void printFields(List<CField> list, String modifier) {
        if (list.size() > 0) {
            line(modifier);
            up();
            for (CField cf : list) {
                setTo(cf);
                append(cf);
            }
            down();
        }
    }

    public void printDestructor() {
        //todo
        append("virtual ~").append(name).append("(){}");
    }


    public boolean hasField(String fname) {
        for (CField cf : fields) {
            if (cf.name.equals(fname)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasFieldAny(String fname) {
        for (CField cf : fields) {
            if (cf.name.equals(fname)) {
                return true;
            }
        }
        for (CClass cc : classes) {
            if (cc.hasFieldAny(fname)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasMethodAny(String mname) {
        for (CMethodDecl cm : methods) {
            if (cm.name.equals(mname)) {
                return true;
            }
        }
        for (CClass cc : classes) {
            if (cc.hasMethodAny(mname)) {
                return true;
            }
        }
        return false;
    }

    int anonyCount = 0;

    public String getAnonyName() {
        return "anony" + anonyCount++;
    }

    Namespace getNs() {
        if (ns != null) {
            return ns;
        }
        return parent.getNs();
    }

    public CType getType() {
        if (type == null) {
            return new CType(getNs().all + "::" + name, header);
        }
        return type;
    }

    CType type;
}
