package com.mesut.j2cpp.ast;

import com.mesut.j2cpp.cppast.stmt.CBlockStatement;

import java.util.ArrayList;
import java.util.List;

public class CMethod extends ModifierNode {

    public String name;
    public CType type;//return type
    public Template template = new Template();
    public List<CParameter> params = new ArrayList<>();
    public boolean isCons = false;//is constructor
    public CClass parent;
    public Call superCall;//super(args)
    public Call thisCall;//this(args)
    public CBlockStatement body;

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
        if (!isCons && type != null) {
            if (isStatic()) {
                append("static ");
            }
            if (parent.isInterface) {
                append("virtual ");
            }
            append(type.toString());
            append(" ");
        }
        /*if (parent.parent != null) {
            append(parent.name + "::");
        }*/
        append(parent.name + "::");

        append(name);
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
