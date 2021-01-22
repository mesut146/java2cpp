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
    public String toString() {
        getScope(scope, name);
        getScope(arguments);
        StringBuilder sb = new StringBuilder();
        if (scope != null) {
            sb.append(scope);
            if (isArrow) {
                sb.append("->");
            }
            else {
                sb.append("::");
            }
        }
        sb.append(name);
        sb.append("(").append(PrintHelper.joinStr(arguments, ", ")).append(")");
        return sb.toString();
    }

}
