package com.mesut.j2cpp.ast;

import com.mesut.j2cpp.cppast.CExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import java.util.ArrayList;
import java.util.List;

public class CField extends ModifierNode {

    public CType type;
    public CName name;
    public CClass parent;
    public boolean ptrOnType = false;
    public VariableDeclarationFragment node;//normal field
    public List<CExpression> enumArgs = new ArrayList<>();

    public void setName(String name) {
        this.name = CName.from(name);
    }

    public void print() {
        name.isPointer = type.isPointer();
        if (isStatic()) {
            append("static ");
        }

        append(type.toString());
        append(" ");
        append(name.toString());
        append(";");
    }


}
