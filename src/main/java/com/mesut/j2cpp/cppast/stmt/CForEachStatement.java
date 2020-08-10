package com.mesut.j2cpp.cppast.stmt;

import com.mesut.j2cpp.cppast.CExpression;
import com.mesut.j2cpp.cppast.CStatement;

public class CForEachStatement extends CStatement {
    CExpression left;//parameter
    CExpression right;//expression
    CStatement body;
}
