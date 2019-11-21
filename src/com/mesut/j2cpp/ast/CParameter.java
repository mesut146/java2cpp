package com.mesut.j2cpp.ast;
import java.io.*;

public class CParameter extends Node
{
    public String type;
    public String name;
    public boolean isPointer=true;

    public void print()
    {
        append(type.replace(".","::"));//normalize the type(base::type)
        if(isPointer){
            append("*");
        }
        append(" ");
        append(name);
    }
    
    
}
