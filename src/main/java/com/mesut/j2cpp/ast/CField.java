package com.mesut.j2cpp.ast;

import com.mesut.j2cpp.cppast.CExpression;

import java.util.ArrayList;
import java.util.List;

public class CField extends ModifierNode {

    public CType type;
    public CName name;
    public CClass parent;
    public boolean ptrOnType = false;
    public CExpression expression;
    public List<CExpression> enumArgs = new ArrayList<>();

    public void setName(String name) {
        this.name = CName.from(name);
    }

    //for header
    public void print() {
        name.isPointer = type.isPointer();
        if (isStatic()) {
            append("static ");
        }

        append(type.toString());
        append(" ");
        append(name.toString());
        if (!isStatic() && expression != null) {
            append(" = ");
            append(expression.toString());
        }
        append(";");
    }

    public String forSource() {
        StringBuilder sb = new StringBuilder();
        sb.append(type.toString());
        sb.append(" ");
        sb.append(parent.name);
        sb.append("::");
        sb.append(name);
        if (expression != null) {//static
            sb.append(" = ");
            sb.append(expression.toString());
        }
        sb.append(";");
        return sb.toString();
    }


}
