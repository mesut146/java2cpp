package com.mesut.j2cpp.cppast.stmt;

import com.mesut.j2cpp.cppast.CStatement;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/*
{
 stmt1
 stmt2
}
 */
public class CBlockStatement extends CStatement {
    public List<CStatement> statements = new ArrayList<>();

    public void print() {
        Iterator<CStatement> it = statements.iterator();

        while (it.hasNext()) {
            //print(it.next)
        }
    }
}
