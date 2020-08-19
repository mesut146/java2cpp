package com.mesut.j2cpp.cppast.expr;

import com.mesut.j2cpp.ast.CName;
import com.mesut.j2cpp.cppast.CExpression;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CMethodInvocation extends CExpression {
    public CExpression scope;
    public CName name;
    public List<CExpression> arguments = new ArrayList<>();
    public boolean isArrow;//pointer or dot

    @Override
    public void print() {
        if (scope != null) {
            append(scope.toString());
            if (isArrow) {
                append("->");
            }
            else {
                append("::");
            }
        }
        append(name.toString());
        append("(");
        append(arguments.stream().map(CExpression::toString).collect(Collectors.joining(", ")));
        append(")");
    }
}
