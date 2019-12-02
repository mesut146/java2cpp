package com.mesut.j2cpp.ast;
import java.util.*;
import java.io.*;
import com.mesut.j2cpp.*;

public class CMethod extends Node
{
    public String name;
    public TypeName type;
    public List<CParameter> params=new ArrayList<>();
    public List<String> modifiers=new ArrayList<>();
    public List<String> throwList=new ArrayList<>();
    public boolean empty=false;
    public boolean isCons=false;
    public CClass parent;
    public Nodew body=new Nodew();
    //public Nodew decl;
    
    public void print()
    {
        list.clear();
        
        printDecl();
        
        if(parent.inHeader){
            append(";");
        }
        else{
            append(body);
            println();
        }
        
    }
    
    public void printDecl(){
        if(!isCons){
            if(isStatic()){
                append("static ");
            }
            append(type.toString());
            if(isPointer()&&!type.isArray()){
                append("*");
            }
            append(" ");
        }
        if(!parent.inHeader){
            append(parent.getNamespaceFull()+"::");
        }

        append(name);
        append("(");
        for(int i=0;i<params.size();i++){
            CParameter cp=params.get(i);
            append(cp.toString());
            if(i<params.size()-1){
                append(",");
            }
        }
        append(")");
    }
    
    boolean isPointer(){
        return !isCons&&!Helper.is(type.toString())&&!type.toString().equals("void");
    }
    
    boolean isVoid(){
        return !isCons&&type.isVoid();
    }
    
    boolean isStatic(){
        return modifiers.contains("static");
    }
    
}
