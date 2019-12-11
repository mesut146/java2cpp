package com.mesut.j2cpp.ast;
import com.mesut.j2cpp.*;

public class TypeName
{
    public Namespace ns;
    public String type;
    public int arrayLevel=0;
    
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
        return arrayLevel>0;
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
        if (isArray()){
           return strLevel(arrayLevel);
        }
        return type;
    }

    String strLevel(int level){
        if (level==0){
            return type;
        }
        return "java_array<"+strLevel(level-1)+">";
    }
    
}
