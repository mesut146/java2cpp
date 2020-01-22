package com.mesut.j2cpp.ast;

import com.mesut.j2cpp.Nodew;

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
    public boolean isInterface = false, isEnum = false;
    public CClass parent;
    public Namespace ns = null;
    public boolean forHeader = true;
    public Nodew staticBlock = null;
    //public boolean inHeader=false;

    public void addInner(CClass cc) {
        cc.parent = this;
        //cc.inHeader=inHeader;
        classes.add(cc);
    }

    public void addMethod(CMethod cm) {
        cm.parent = this;
        methods.add(cm);
    }

    public void addField(CField cf) {
        fields.add(cf);
    }

    public Namespace getNamespace() {
        String str;
        if (parent == null) {//header level
            return ns;
        }
        str = parent.getNamespace().all + "::" + name;
        Namespace n = new Namespace();
        n.all = (str);
        return n;
    }

    public Namespace getNamespaceFull() {
        String str;
        Namespace n = new Namespace();
        if (parent != null) {
            str = parent.getNamespace().all + "::";
            n.pkg(str);
        } else if (ns != null) {
            n.pkg(ns.all + "::" + name);
        }
        return n;
    }

    public void print() {
        if (parent == null && ns != null) {
            line("namespace ");
            append(ns.all);
            appendln("{");
            up();
        }
        if (template.list.size() > 0) {
            println();
            append(template.toString());
        }
        line("class ");
        append(name);
        if (base.size() > 0) {
            append(":");
            for (int i = 0; i < base.size(); i++) {
                append(base.get(i).toString());
                if (i < base.size() - 1) {
                    append(",");
                }
            }
        }
        append("{");
        up();
        if (staticBlock != null) {
            println();
            println();
            appendi(staticBlock);
            println();
        }

        List<CField> fpub = fields.stream().filter(CField::isPublic).collect(Collectors.toList());
        if (fpub.size() > 0) {
            line("public:");
            up();
            for (CField cf : fpub) {
                setTo(cf);
                append(cf);
            }
            down();
        }
        List<CField> fpriv = fields.stream().filter(CField::isPrivate).collect(Collectors.toList());
        if (fpriv.size() > 0) {
            line("private:");
            up();
            for (CField cf : fpriv) {
                setTo(cf);
                append(cf);
            }
            down();
        }

        println();
        List<CMethod> mpub = methods.stream().filter(CMethod::isPublic).collect(Collectors.toList());
        if (mpub.size() > 0) {
            line("public:");
            up();
            for (CMethod cm : mpub) {
                setTo(cm);
                append(cm);
            }
            down();
        }
        List<CMethod> mpriv = methods.stream().filter(CMethod::isPrivate).collect(Collectors.toList());
        if (mpriv.size() > 0) {
            line("private:");
            up();
            for (CMethod cm : mpriv) {
                setTo(cm);
                append(cm);
            }
            down();
        }
        println();
        for (CClass cc : classes) {
            setTo(cc);
            append(cc);
        }
        down();
        lineln("};//class " + name);

        if (parent == null && ns != null) {
            down();
            appendln("}//ns");
        }
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
