package com.mesut.j2cpp.cppast.expr;

import com.mesut.j2cpp.Config;
import com.mesut.j2cpp.ast.CType;
import com.mesut.j2cpp.cppast.CExpression;

//(targetType)expression
public class CCastExpression extends CExpression {
    public CType targetType;
    public CExpression expression;

    public void setTargetType(CType targetType) {
        this.targetType = targetType.copy();
        this.targetType.setPointer(Config.ptr_cast);
    }

    @Override
    public void print() {
        append("(");
        append(targetType);
        append(")");
        append(expression.toString());
    }
}
