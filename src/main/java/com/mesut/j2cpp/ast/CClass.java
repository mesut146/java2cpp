package com.mesut.j2cpp.ast;

import com.mesut.j2cpp.Writer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CClass extends Node {

    public String name;
    public List<CType> base = new ArrayList<>();
    public Template template = new Template();
    public List<CField> fields = new ArrayList<>();
    public List<CMethod> methods = new ArrayList<>();
    public List<CClass> classes = new ArrayList<>();
    public boolean isInterface = false;
    public boolean isEnum = false;//todo
    public CClass parent;//outer
    public Namespace ns = null;
    public boolean forHeader = true;
    public Writer staticBlock = null;

    public void addInner(CClass cc) {
        cc.parent = this;
        classes.add(cc);
    }

    public void addMethod(CMethod cm) {
        cm.parent = this;
        methods.add(cm);
    }

    public void addField(CField cf) {
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
        Namespace n = new Namespace();
        n.all = str;
        return n;
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
        printFields();
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
                append(base.get(i).toString());
                if (i < base.size() - 1) {
                    append(",");
                }
            }
        }
    }

    private void printMethods(List<CMethod> list, String modifier) {
        if (list.size() > 0) {
            line(modifier);
            up();
            for (CMethod cm : list) {
                setTo(cm);
                append(cm);
            }
            down();
        }
    }

    private void printMethods() {
        List<CMethod> public_methods = methods.stream().filter(CMethod::isPublic).collect(Collectors.toList());
        List<CMethod> priv_methods = methods.stream().filter(CMethod::isPrivate).collect(Collectors.toList());
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

    //return this class as type ,in hierarchy ,e.g org::MyClass::Inner::inner_field
    public CType asType() {
        CType type = new CType(name);
        type.ns = ns;
        if (parent != null) {
            type.scope = parent.asType();
        }
        return type;
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
        for (CMethod cm : methods) {
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
}
