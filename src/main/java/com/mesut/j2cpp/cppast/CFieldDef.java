package com.mesut.j2cpp.cppast;

import com.mesut.j2cpp.ast.CField;
import com.mesut.j2cpp.ast.CName;
import com.mesut.j2cpp.ast.CType;

public class CFieldDef extends CNode {
    CField field;
    CExpression expression;

    public CFieldDef(CField field) {
        this.field = field;
    }

    public CType getType() {
        return field.type;
    }

    public CName getName() {
        return field.name;
    }

    public void setExpression(CExpression expression) {
        this.expression = expression;
    }

    public boolean hasExpression() {
        return expression != null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getType());
        sb.append(" ");
        sb.append(field.parent.name);
        sb.append("::");
        sb.append(getName());
        if (expression != null) {
            sb.append(" = ");
            sb.append(expression);
        }
        sb.append(";");
        return sb.toString();
    }
}
