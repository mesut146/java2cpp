package com.mesut.j2cpp.ast;

import com.mesut.j2cpp.Nodew;

import java.util.ArrayList;
import java.util.List;

public class CMethod extends ModifierNode {
    public String name;
    public CType type;
    public Template template = new Template();
    public List<CParameter> params = new ArrayList<>();
    public List<String> throwList = new ArrayList<>();
    public boolean empty = false;
    public boolean isCons = false;
    public CClass parent;
    public Call call;
    public Nodew body = new Nodew();
    //public Nodew decl;

    public CClass getParent() {
        return parent;
    }

    public Template getTemplate() {
        return template;
    }

    public String getName() {
        return name;
    }

    public CType getType() {
        return type;
    }

    public void print() {
        list.clear();

        if (isNative()) {
            //System.out.println("native method");
            appendln("/*TODO native*/ ");
            append("extern ");
            /*
             *extern to get rid of compilation errors
             *later they will be implemented or,linked from openjdk
             */
        }
        printDecl();
        if (parent.isInterface) {//make it virtual
            append("=0");
        }
        if (parent.forHeader) {
            append(";");
        } else {
            if (call != null) {
                append(":");
                append(call.str);
            }
            append(body);
            println();
        }

    }

    public void printDecl() {
        if (!isCons) {
            if (isStatic()) {
                append("static ");
            }
            if (parent.isInterface) {
                append("virtual ");
            }
            append(type.toString());
            if (isPointer()) {
                append("*");
            }
            append(" ");
        }
        if (!parent.forHeader && parent.ns != null) {
            append(parent.getNamespaceFull().all + "::");
        }

        append(name);
        append("(");
        for (int i = 0; i < params.size(); i++) {
            CParameter cp = params.get(i);
            append(cp.toString());
            if (i < params.size() - 1) {
                append(",");
            }
        }
        append(")");
    }

    boolean isPointer() {
        return !isCons && type.isPointer();
    }

    boolean isVoid() {
        return !isCons && type.isVoid();
    }


}
