package com.mesut.j2cpp.cppast.expr;

import com.mesut.j2cpp.ast.CName;
import com.mesut.j2cpp.cppast.CExpression;
import com.mesut.j2cpp.util.PrintHelper;

import java.util.ArrayList;
import java.util.List;

public class CMethodInvocation extends CExpression {
    public CExpression scope;
    public CName name;
    public List<CExpression> arguments = new ArrayList<>();
    public boolean isArrow;//pointer or dot

    @Override
    public void print() {
        getScope(scope, name);
        getScope(arguments);
        if (scope != null) {
            append(scope);
            if (isArrow) {
                append("->");
            }
            else {
                append("::");
            }
        }
        name.scope = this.scope;
        append(name.toString());
        append("(");
        PrintHelper.join(this, arguments, ", ", this.scope);
        append(")");
    }
}
