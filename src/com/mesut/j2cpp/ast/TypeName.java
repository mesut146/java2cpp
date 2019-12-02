package com.mesut.j2cpp.ast;
import com.mesut.j2cpp.*;

public class TypeName
{
    public Namespace ns;
    public String type;
    
    public TypeName(String s){
        type=s;
    }
    
    public String full(){
        if(ns==null){
            return type;
        }
        return ns.all+"::"+type;
    }
    
    public boolean isArray(){
        return type.endsWith("[]");
    }
    
    public boolean isPointer(){
        return !isVoid()&&!isArray()&&!Helper.is(type);
    }

    public boolean isVoid(){
        return type.equals("void");
    }
    
    @Override
    public String toString()
    {
        return type;
    }
    
    
}
