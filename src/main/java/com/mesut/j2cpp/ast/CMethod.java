package com.mesut.j2cpp.ast;

import com.mesut.j2cpp.Config;
import com.mesut.j2cpp.cppast.stmt.CBlockStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
            append(type);
            append(" ");
        }
        if (!parent.isAnonymous && source) {
            append(parent.name + "::");
        }
        append(name.name);

        append("(");
        append(params.stream().map(CParameter::toString).collect(Collectors.joining(", ")));
        append(")");
        if (source) {
            if (superCall != null) {
                append(" : ");
                append(superCall.toString());
            }
            if (thisCall != null) {
                if (superCall != null) {
                    append(", ");
                }
                else {
                    append(" : ");
                }
                append(thisCall.toString());
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

    public void printAnony() {

    }


}
