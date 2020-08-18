package com.mesut.j2cpp.ast;

import org.eclipse.jdt.core.dom.MethodDeclaration;

import java.util.ArrayList;
import java.util.List;

public class CMethodDecl extends ModifierNode {

    public CType type;//return type
    public CName name;
    public Template template = new Template();
    public List<CParameter> params = new ArrayList<>();
    public boolean isCons = false;//is constructor
    public boolean isPureVirtual = false;
    public CClass parent;
    public MethodDeclaration node;

    @Override
    public void print() {
        list.clear();
        if (!template.isEmpty()) {
            lineln(template.toString());
        }
        if (!isCons) {
            append(type);
            append(" ");
        }
        append(name.name);
        append("(");
        for (int i = 0; i < params.size(); i++) {
            append(params.get(i).toString());
            if (i < params.size() - 1) {
                append(", ");
            }
        }
        append(")");
        if (isPureVirtual) {
            append(" = 0");
        }
        append(";");
    }
}
