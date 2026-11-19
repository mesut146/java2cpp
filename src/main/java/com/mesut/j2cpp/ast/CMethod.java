package com.mesut.j2cpp.ast;

import com.mesut.j2cpp.Config;
import com.mesut.j2cpp.cppast.stmt.CBlockStatement;
import com.mesut.j2cpp.map.ClassMap;
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
    //explicit inline body for generated stubs (full text after the signature)
    public String inlineBody = null;

    private String baseInitializer() {
        if (parent == null || parent.superClass == null) return "";
        CClass base = ClassMap.sourceMap.get(parent.superClass);
        if (base == null) return "";
        CMethod baseCons = base.firstConstructor();
        if (baseCons == null) return "";
        StringBuilder sb = new StringBuilder(" : ").append(parent.superClass).append("(");
        for (int i = 0; i < baseCons.params.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(i < params.size() ? params.get(i).name.name : "{}");
        }
        return sb.append(")").toString();
    }

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
        else if (parent != null && !parent.fromSource && template.isEmpty() && body == null) {
            //library stubs are header-only: give every declaration a definition
            //so generated code can link without hand-written implementations
            if (inlineBody != null) {
                sb.append(" ").append(inlineBody);
            }
            else if (isCons) {
                sb.append(baseInitializer()).append(" {}");
            }
            else {
                sb.append(" { ");
                if (type != null && !type.isVoid()) sb.append("return {}; ");
                sb.append("}");
            }
        }
        else {
            sb.append(";");
        }
        return sb.toString();
    }


}
