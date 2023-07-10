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
            sb.append(PrintHelper.joinStr(initializers, ", "));
        }
        sb.append(";");
        if (condition != null) {
            sb.append(condition);
        }
        sb.append(";");
        if (!updaters.isEmpty()) {
            sb.append(PrintHelper.joinStr(updaters, ", "));
        }
        sb.append(")");
        if (body == null) {
            sb.append(";");
        }
        else {
            sb.append("\n").append(body);
        }
        return sb.toString();
    }
}
