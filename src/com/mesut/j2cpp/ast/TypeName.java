package com.mesut.j2cpp.ast;

public class TypeName
{
    public Namespace ns;
    public String type;
    
    public TypeName(String s){
        type=s;
    }
    
    public String full(){
        return ns.all+"::"+type;
    }
    
    public boolean isArray(){
        return type.endsWith("[]");
    }

    @Override
    public String toString()
    {
        return type;
    }
    
    
}
