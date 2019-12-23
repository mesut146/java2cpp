package com.mesut.j2cpp.ast;
import com.mesut.j2cpp.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TypeName
{
    public Namespace ns;
    public String type;
    public int arrayLevel=0;
    public List<TypeName> typeNames=new ArrayList<>();
    
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

    public boolean isPrim(){
        return Helper.isPrim(type);
    }
    
    public boolean isPointer(){
        return !isVoid()&&(!isPrim()||isArray());
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
        if (typeNames.size()>0){
            StringBuilder sb = new StringBuilder();
            sb.append(type);
            sb.append("<");
            for (Iterator<TypeName> iterator=typeNames.iterator();iterator.hasNext();){
                sb.append(iterator.next());
                if (iterator.hasNext()) {
                    sb.append(",");
                }
            }
            sb.append(">");
            return sb.toString();
        }
        return type;
    }

    String strLevel(int level){
        if (level==0){
            return type;
        }else if(level==1){
            return "java_array_single<"+strLevel(level-1)+">";
        }
        return "java_array<"+strLevel(level-1)+">";
    }
    
}
