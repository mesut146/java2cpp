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
    public Body body=new Body(){{level=1;init();}};
    public FWriter decl;
    
    public void print()
    {
        baos.reset();
        if(decl==null){
            decl=new FWriter();
            decl.line("");
            if(!isCons){
                decl.append(type.toString());
                if(isPointer()&&!type.isArray()){
                    decl.append("*");
                }
                decl.append(" ");
            }
            decl.append(name);

            decl.append("(");
            for(int i=0;i<params.size();i++){
                CParameter cp=params.get(i);
                decl.append(cp.toString());
                if(i<params.size()-1){
                    decl.append(",");
                }
            }
            decl.append(")");
        }
        
        append(decl.toString());
        
        if(parent.inHeader){
            appendln(";");
        }
        else{
            //appendln("{");
            //TODO
            append(body.toString());
            println();
            //lineln("}");
        }
        
    }
    
    boolean isPointer(){
        return !isCons&&!Helper.is(type.toString())&&!type.toString().equals("void");
    }
    
    
    
}
