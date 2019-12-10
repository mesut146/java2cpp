package com.mesut.j2cpp.ast;
import com.github.javaparser.ast.body.*;
import java.util.*;

public class Call
{
    public boolean isThis;
    public String str;
    public List<Parameter> args=new ArrayList<>();
}
