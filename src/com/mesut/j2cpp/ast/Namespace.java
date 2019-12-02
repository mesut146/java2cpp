package com.mesut.j2cpp.ast;
import java.util.*;

public class Namespace extends Node
{
    public String all;
    public List<String> split=new ArrayList<>();
    
    public Namespace(String ns){
        all=ns.replace(".","::");
    }

    public Namespace(){

    }

    public void pkg(String str){
        int i=0;
        all=str.replace(".","::");
        for(String ns:str.split("::")){
            split.add(ns);
        }
        
    }
    
    public Namespace append(String str){
        if (all==null||all.length()==0){
            return new Namespace(str);
        }
        return new Namespace(all+"::"+str);
    }
    @Override
    public void print()
    {
        
    }
    
    
    
}
