package com.mesut.j2cpp.cppast.stmt;

import com.mesut.j2cpp.cppast.CExpression;
import com.mesut.j2cpp.cppast.CStatement;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

//for(init;cond;update) stmt
public class CForStatement extends CStatement {
    public List<CExpression> initializers = new ArrayList<>();
    public CExpression condition;
    public List<CExpression> updaters = new ArrayList<>();
    public CStatement body;


    @Override
    public void print() {
        append("for(");
        append(";");
        if (condition != null) {
            append(condition.toString());
        }
        append(";");
        if (!updaters.isEmpty()) {
            append(updaters.stream().map(CExpression::toString).collect(Collectors.joining(", ")));
        }
        append(")");
        append(body.toString());
    }
}
