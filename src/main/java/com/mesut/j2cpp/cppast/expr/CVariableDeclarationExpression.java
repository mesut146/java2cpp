package com.mesut.j2cpp.cppast.expr;

import com.mesut.j2cpp.Config;
import com.mesut.j2cpp.ast.CType;
import com.mesut.j2cpp.cppast.CExpression;
import com.mesut.j2cpp.cppast.stmt.CVariableDeclarationFragment;
import com.mesut.j2cpp.util.PrintHelper;

import java.util.ArrayList;
import java.util.List;

public class CVariableDeclarationExpression extends CExpression {
    public CType type;
    public List<CVariableDeclarationFragment> fragments = new ArrayList<>();

    @Override
    public String toString() {
        getScope(type);
        getScope(fragments);
        return type + " " + PrintHelper.joinStr(fragments, ", ");
    }
}
