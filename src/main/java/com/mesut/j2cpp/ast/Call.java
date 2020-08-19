package com.mesut.j2cpp.ast;


import com.mesut.j2cpp.cppast.CExpression;

import java.util.ArrayList;
import java.util.List;

//super call in cons
public class Call {
    public boolean isThis;
    public String str;
    public List<CExpression> args = new ArrayList<>();
}
