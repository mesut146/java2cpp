package com.mesut.j2cpp.ast;
import java.util.*;
import java.io.*;

public class CField extends Node
{
    public String type,name,right;
    public boolean isPublic=false,isStatic=false;
    public boolean isPointer=false;
    
    
    public void print(){
        print("");
        if(isPublic){
            append("public: ");
        }
        append(type);
        append(" ");
        append(name);
        append(";\n");
        //TODO right
    }
}
