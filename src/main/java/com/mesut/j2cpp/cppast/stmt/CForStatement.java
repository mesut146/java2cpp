package com.mesut.j2cpp.cppast.stmt;

import com.mesut.j2cpp.cppast.CExpression;
import com.mesut.j2cpp.cppast.CStatement;

import java.util.List;

//for(init;cond;update) stmt
public class CForStatement extends CStatement {
    List<CExpression> initializers;
    CExpression condition;
    List<CExpression> updaters;
    CStatement body;
}
