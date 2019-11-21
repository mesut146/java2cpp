package com.mesut.j2cpp.ast;
import java.util.*;
import java.io.*;
import com.mesut.j2cpp.*;

public class CMethod extends Node
{
    public String name;
    public String type;
    public List<CParameter> params=new ArrayList<>();
    public List<String> modifiers=new ArrayList<>();
    public List<String> throwList=new ArrayList<>();
    public boolean empty=false;
    public boolean isCons=false;
    //public MethodVisitor visitor;
    public CClass parent;
    public Body body=new Body();
    
    public void print()
    {
        line("");
        if(!isCons){
            append(type).
            append(" ");
        }
        append(name);
        if(isPointer()){
            append("*");
        }
        append("(");
        for(int i=0;i<params.size();i++){
            CParameter cp=params.get(i);
            append(cp.toString());
            if(i<params.size()-1){
                append(",");
            }
        }
        append(")");
        
        if(parent.inHeader){
            appendln(";");
        }
        else{
            appendln("{");
            //TODO
            append(body.toString());
            lineln("}");
        }
        
    }
    
    boolean isPointer(){
        return !isCons&&!Helper.is(type);
    }
    
    
}
