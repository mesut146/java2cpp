package com.mesut.j2cpp.cppast.stmt;

import com.mesut.j2cpp.cppast.CExpression;
import com.mesut.j2cpp.cppast.CStatement;

import java.sql.Statement;

public class CIfStatement extends CStatement {
    CExpression condition;
    CStatement thenStatement;
    Statement elseStatement;
}
