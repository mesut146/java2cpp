package com.mesut.j2cpp.cppast.expr;

import com.mesut.j2cpp.ast.CType;
import com.mesut.j2cpp.cppast.CExpression;
import com.mesut.j2cpp.cppast.stmt.CVariableDeclarationFragment;

import java.util.ArrayList;
import java.util.List;

public class CVariableDeclarationExpression extends CExpression {
    public CType type;
    public List<CVariableDeclarationFragment> fragments = new ArrayList<>();


    @Override
    public void print() {
        append(type);
        append(" ");
        for (int i = 0; i < fragments.size(); i++) {
            append(fragments.get(i).toString());
            if (i < fragments.size() - 1) {
                append(", ");
            }
        }
    }
}
