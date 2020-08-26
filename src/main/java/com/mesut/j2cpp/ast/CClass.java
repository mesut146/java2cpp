package com.mesut.j2cpp.ast;

import com.mesut.j2cpp.Config;
import com.mesut.j2cpp.Writer;
import com.mesut.j2cpp.cppast.CStatement;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CClass extends CStatement {

    public String name;
    public boolean isInterface = false;
    public Namespace ns = null;
    public List<CType> base = new ArrayList<>();
    public Template template = new Template();
    public List<CField> fields = new ArrayList<>();
    public List<CMethod> methods = new ArrayList<>();
    public List<CClass> classes = new ArrayList<>();
    public CClass parent;//outer
    public CHeader header;
    public Writer staticBlock = null;
    public boolean isAnonymous = false;

    public CClass() {
        if (Config.baseClassObject) {
            base.add(new CType("java::lang::Object"));
        }
    }

    public void addInner(CClass cc) {
        cc.parent = this;
        cc.header = header;
        cc.ns = ns;
        classes.add(cc);
    }

    public void addMethod(CMethod cm) {
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

    public CType getSuper() {
        if (base.isEmpty()) {
            return null;
        }
        return base.get(0);
    }

    public void print() {
        clear();
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
        if (isInterface) {
            line("/*interface*/");
        }
        if (!template.isEmpty()) {
            println();
            append(template.toString());
        }
        //class decl
        line("class ");
        append(name);
        if (base.size() > 0) {
            append(": public ");
            append(base.stream().map(CType::toString).collect(Collectors.joining(" ,")));
        }
    }

    private void printMethods() {
        if (!methods.isEmpty()) {
            line("//methods");
        }
        List<CMethod> public_methods = methods.stream().filter(CMethod::isPublic).collect(Collectors.toList());
        List<CMethod> priv_methods = methods.stream().filter(CMethod::isPrivate).collect(Collectors.toList());
        printMethods(public_methods, "public:");
        printMethods(priv_methods, "private:");
        println();
    }

    private void printMethods(List<CMethod> list, String modifier) {
        if (list.size() > 0) {
            line(modifier);
            up();
            for (CMethod cm : list) {
                cm.printAll(false);
                appendIndent(cm);
            }
            down();
        }
    }

    private void printFields() {
        if (!fields.isEmpty()) {
            line("//fields");
        }
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
                appendIndent(cf);
            }
            down();
        }
    }

    public void printDestructor() {
        //todo
        CMethod destructor = new CMethod();
        destructor.parent = this;
        destructor.name = new CName("~" + name);
        destructor.setVirtual(true);
        append("virtual ~").append(name).append("(){}");
    }


    int anonyCount = 0;
    CType type;

    public String getAnonyName() {
        return "anony" + anonyCount++;
    }

    Namespace getNs() {
        if (ns != null) {
            return ns;
        }
        if (parent != null) {
            return parent.getNs();
        }
        return null;
    }

    public CType getType() {
        if (type == null) {
            return new CType(getNs().all + "::" + name, header);
        }
        return type;
    }


}
