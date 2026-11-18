package com.mesut.j2cpp.ast;

import com.mesut.j2cpp.Config;
import com.mesut.j2cpp.cppast.stmt.CBlockStatement;
import com.mesut.j2cpp.util.PrintHelper;

import java.util.ArrayList;
import java.util.List;

public class CMethod extends ModifierNode {

    public CType type;//return type
    public CName name;
    public Template template = new Template();
    public List<CParameter> params = new ArrayList<>();
    public boolean isCons = false;//is constructor
    public boolean isPureVirtual = false;
    public boolean isOverride = false;
    public CClass parent;
    public CBlockStatement body;

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
        getScope(template, body);
        getScope(params);
        StringBuilder sb = new StringBuilder();
        if (!template.isEmpty()) {
            getScope(template);
            sb.append(template.toString()).append("\n");
        }
        if (!isCons) {
            type = type.copy();
            type.setPointer(Config.ptr_method);
            getScope(type);
            if (isStatic()) {
                sb.append("static ");
            }
            if (isVirtual() && !isStatic() && template.isEmpty()) {
                sb.append("virtual ");
            }
            sb.append(type);
            sb.append(" ");
        }
        sb.append(name.name);

        //params
        sb.append("(").append(PrintHelper.joinStr(params, ", ")).append(")");

        if (isOverride) {
            sb.append(" override");
        }
        if (isPureVirtual && !isStatic()) {
            sb.append(" = 0;");
        }
        else if (parent != null && !parent.fromSource && is(VIRTUAL_NAME) && !isStatic()
                && template.isEmpty() && body == null) {
            //library stub methods get an inline body so generated subclasses
            //can call or inherit them without a separate definition
            sb.append(" { ");
            if (!type.isVoid()) sb.append("return {}; ");
            sb.append("}");
        }
        else {
            sb.append(";");
        }
        return sb.toString();
    }


}
