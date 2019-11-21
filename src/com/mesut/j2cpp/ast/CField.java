package com.mesut.j2cpp.ast;
import com.mesut.j2cpp.*;

public class CField extends Node
{
    public String type,name,right;
    public boolean isPublic=false,isStatic=false;
    
    
    public void print(){
        line("");
        if(isPublic){
            append("public: ");
        }
        append(type);
        if(isPointer()){
            append("*");
        }
        append(" ");
        append(name);
        appendln(";");
        //TODO right
    }
    
    boolean isPointer(){
        return !Helper.is(type);
    }
}
