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
        print("");
        if(!isCons){
            append(type);
            append(" ");
        }
        append(name);
        append("(");
        for(int i=0;i<params.size();i++){
            CParameter cp=params.get(i);
            cp.print();
            append(cp.toString());
            if(i<params.size()-1){
                append(",");
            }
        }
        append(")");
        
        if(parent.inHeader){
            append(";\n");
        }
        else{
            append("{\n");
            //TODO
            append(body.toString());
            println("}");
        }
        
    }
    public void getDecl(PrintWriter pw){
        print("");
        if(!isCons){
            pw.append(type);
            pw.append(" ");
        }
        append(name);
        append("(");
        for(int i=0;i<params.size();i++){
            CParameter cp=params.get(i);
            cp.print();
            append(cp.toString());
            if(i<params.size()-1){
                append(",");
            }
        }
        append(")");

        if(parent.inHeader){
            println(";");
        }
    }
    
    
}
