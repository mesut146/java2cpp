package com.mesut.j2cpp.ast;
import java.io.*;

public class CParameter extends Node
{
    public TypeName type;
    public String name;
    //public boolean isPointer=true;

    public void print()
    {
        list.clear();
        if (type==null){
            System.out.println("name="+name);
        }
        append(type.toString().replace(".","::"));//normalize the type(base::type)
        if(type.isPointer()){
            append("*");
        }
        append(" ");
        append(name);
    }
    
    
}
