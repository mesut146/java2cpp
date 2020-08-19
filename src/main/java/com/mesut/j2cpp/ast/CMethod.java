package com.mesut.j2cpp.ast;

import com.mesut.j2cpp.cppast.stmt.CBlockStatement;

import java.util.ArrayList;
import java.util.List;

public class CMethod extends ModifierNode {

    public CMethodDecl decl;
    public Template template = new Template();
    public List<CParameter> params = new ArrayList<>();
    public Call superCall;//super(args)
    public Call thisCall;//this(args)
    public CBlockStatement body;

    public CClass getParent() {
        return decl.parent;
    }

    public Template getTemplate() {
        return template;
    }

    public String getName() {
        return decl.name.name;
    }

    public CType getType() {
        return decl.type;
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

        if (superCall != null) {
            append(":");
            append(superCall.str);
        }
        if (thisCall != null) {
            if (superCall != null) {
                append(", ");
            }
            append(thisCall.str);
        }
        append(body.toString());
        println();
    }

    public void printDecl() {
        //some enums have null type for some reason
        if (!decl.isCons && getType() != null) {
            if (isStatic()) {
                append("static ");
            }
            if (getParent().isInterface) {
                append("virtual ");
            }
            append(getType().toString());
            append(" ");
        }
        /*if (parent.parent != null) {
            append(parent.name + "::");
        }*/
        append(decl.parent.name + "::");

        append(getName());
        append("(");
        for (int i = 0; i < params.size(); i++) {
            CParameter cp = params.get(i);
            append(cp.toString());
            if (i < params.size() - 1) {
                append(", ");
            }
        }
        append(")");
    }


}
