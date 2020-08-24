package com.mesut.j2cpp.cppast.expr;

import com.mesut.j2cpp.ast.CType;
import com.mesut.j2cpp.cppast.CExpression;

//(targetType)expression
public class CCastExpression extends CExpression {
    public CType targetType;
    public CExpression expression;


    @Override
    public void print() {
        append("(");
        append(targetType);
        append(")");
        append(expression.toString());
    }
}
