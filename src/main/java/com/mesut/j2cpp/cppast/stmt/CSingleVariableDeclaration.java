package com.mesut.j2cpp.cppast.stmt;

import com.mesut.j2cpp.ast.CName;
import com.mesut.j2cpp.ast.CType;
import com.mesut.j2cpp.cppast.CExpression;

public class CSingleVariableDeclaration extends CExpression {
    public CType type;
    public CName name;
    public CExpression expression;


    @Override
    public void print() {
        append(type.toString());
        append(" ");
        append(name.toString());
        if (expression != null) {
            append(expression.toString());
        }
        //append(";");
    }
}
