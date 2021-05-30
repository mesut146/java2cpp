package com.mesut.j2cpp.cppast.stmt;

import com.mesut.j2cpp.ast.CName;
import com.mesut.j2cpp.ast.CType;
import com.mesut.j2cpp.cppast.CExpression;

//type name [= expression]
public class CSingleVariableDeclaration extends CExpression {
    public CType type;
    public CName name;
    public CExpression expression;

    @Override
    public String toString() {
        getScope(type, name, expression);
        StringBuilder sb = new StringBuilder();
        sb.append(type);
        sb.append(" ");
        sb.append(name);
        if (expression != null) {
            sb.append(expression);
        }
        return sb.toString();
    }


}
