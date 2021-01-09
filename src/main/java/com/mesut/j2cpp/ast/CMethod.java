package com.mesut.j2cpp.ast;

import com.mesut.j2cpp.Config;
import com.mesut.j2cpp.cppast.stmt.CBlockStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import java.util.ArrayList;
import java.util.List;

public class CMethod extends ModifierNode {

    public CType type;//return type
    public CName name;
    public Template template = new Template();
    public List<CParameter> params = new ArrayList<>();
    public boolean isCons = false;//is constructor
    public boolean isPureVirtual = false;
    public CClass parent;
    public Call superCall;//super(args)
    public Call thisCall;//this(args)
    public CBlockStatement body;
    public MethodDeclaration node;//body node

    public void addParam(CParameter param) {
        param.method = this;
        params.add(param);
    }

    public void setType(CType type) {
        this.type = type.copy();
        this.type.setPointer(Config.ptr_method);
    }

    public CHeader getHeader() {
        return parent.header;
    }

    @Override
    public void print() {

    }

    public void printAll(boolean source) {
        clear();
        if (!template.isEmpty()) {
            lineln(template.toString());
        }
        if (!isCons) {
            if (isStatic() && !source) {
                append("static ");
            }
            if (isVirtual()) {
                append("virtual ");
            }
            if (source) {
                append(type.normalized(getHeader().source));
            }
            else {
                append(type.normalized(getHeader()));
            }
            append(" ");
        }
        if (!parent.isAnonymous && source) {
            append(parent.name + "::");
        }
        append(name.name);

        append("(");
        for (int i = 0; i < params.size(); i ++) {
            CParameter cp = params.get(i);
            if (source) {
                append(cp.printFor(getHeader().source));
            }
            else {
                append(cp.printFor(getHeader()));
            }
            if (i < params.size() - 1)
                append(", ");
        }
        append(")");
        if (source) {
            if (superCall != null) {
                append(" : ");
                append(superCall);
            }
            if (thisCall != null) {
                if (superCall != null) {
                    append(", ");
                }
                else {
                    append(" : ");
                }
                append(thisCall);
            }
            append(body);
        }
        else {
            if (isPureVirtual) {
                append(" = 0");
            }
            append(";");
        }
    }


}
