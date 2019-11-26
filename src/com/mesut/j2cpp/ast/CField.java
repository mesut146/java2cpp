package com.mesut.j2cpp.ast;
import com.mesut.j2cpp.*;

public class CField extends Node
{
    public TypeName type;
    public String name,right;
    public boolean isPublic=false,isStatic=false;
    
    
    public void print(){
        //line("");
        if(isPublic){
            line("public: ");
        }
        append(type.toString());
        if(isPointer()&&!type.isArray()){
            append("*");
        }
        append(" ");
        append(name);
        append(";\n");
        //TODO right
    }
    
    boolean isPointer(){
        return !Helper.is(type.toString());
    }
}
