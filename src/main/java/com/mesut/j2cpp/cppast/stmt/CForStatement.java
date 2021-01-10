package com.mesut.j2cpp.cppast.stmt;

import com.mesut.j2cpp.cppast.CExpression;
import com.mesut.j2cpp.cppast.CStatement;
import com.mesut.j2cpp.util.PrintHelper;

import java.util.ArrayList;
import java.util.List;

//for(init;cond;update) stmt
public class CForStatement extends CStatement {
    public List<CExpression> initializers = new ArrayList<>();
    public CExpression condition;
    public List<CExpression> updaters = new ArrayList<>();
    public CStatement body;

    @Override
    public String toString() {
        getScope(condition, body);
        getScope(initializers, updaters);
        StringBuilder sb = new StringBuilder();
        sb.append("for(");
        if (!initializers.isEmpty()) {
            PrintHelper.join(sb, initializers, ", ", scope);
        }
        sb.append(";");
        if (condition != null) {
            sb.append(condition);
        }
        sb.append(";");
        if (!updaters.isEmpty()) {
            PrintHelper.join(sb, updaters, ", ", scope);
        }
        sb.append(")");
        if (body == null) {
            sb.append(";");
        }
        else {
            sb.append("\n").append(PrintHelper.body(body.toString(), "    "));
        }
        return sb.toString();
    }
}
