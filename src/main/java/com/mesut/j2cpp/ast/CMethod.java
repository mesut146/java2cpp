package com.mesut.j2cpp.ast;

import com.mesut.j2cpp.Config;
import com.mesut.j2cpp.cppast.stmt.CBlockStatement;
import com.mesut.j2cpp.util.PrintHelper;
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
        params.add(param);
    }

    public void setType(CType type) {
        this.type = type;
    }

    public CHeader getHeader() {
        return parent.header;
    }

    public String toString() {
        getScope(template, body, thisCall, superCall);
        getScope(params);
        boolean source = scope instanceof CSource;
        StringBuilder sb = new StringBuilder();
        if (!template.isEmpty()) {
            getScope(template);
            sb.append(template.toString());
        }
        if (!isCons) {
            type = type.copy();
            type.setPointer(Config.ptr_method);
            getScope(type);
            if (isStatic() && !source) {
                sb.append("static ");
            }
            if (isVirtual()) {
                sb.append("virtual ");
            }
            sb.append(type);
            sb.append(" ");
        }
        if (!parent.isAnonymous && source) {
            sb.append(parent.name).append("::");
        }
        sb.append(name.name);

        //params
        sb.append("(").append(PrintHelper.joinStr(params, ", ")).append(")");

        if (source) {
            if (superCall != null) {
                sb.append(" : ");
                sb.append(superCall);
            }
            if (thisCall != null) {
                if (superCall != null) {
                    sb.append(", ");
                }
                else {
                    sb.append(" : ");
                }
                sb.append(thisCall);
            }
            sb.append("\n").append(body);
        }
        else {
            if (isPureVirtual) {
                sb.append(" = 0");
            }
            sb.append(";");
        }
        return sb.toString();
    }


}
