package com.mesut.j2cpp.cppast;

import java.util.List;

//for(init;cond;update) stmt
public class CForStatement extends CStatement{
    List<CExpression> initializers;
    CExpression condition;
    List<CExpression> updaters;
    CStatement body;
}
