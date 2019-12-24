package com.mesut.j2cpp.ast;
import java.util.*;

public class CHeader extends Node
{
    public String name;
    public List<String> includes=new ArrayList<>();
    public List<CClass> classes=new ArrayList<>();
    public Namespace ns;
    public String rpath;
    boolean hasRuntime=false;
    
    public void addClass(CClass cc){
        cc.ns=ns;
        classes.add(cc);
    }

    public void addRuntime(){
        hasRuntime=true;
    }

    public void print()
    {
        append("#pragma once");
        println();
        println();
        for(String imp:includes){
            include(imp);
        }
        if (hasRuntime){
            include("JavaRuntime.h");
        }
        println();
        appendln("using namespace com::java::lang;");
        if(ns!=null){
            append("using namespace ").append(ns.all).appendln(";");
        }
        
        for(CClass cc:classes){
            cc.forHeader=true;
            append(cc);
        }
    }
    
    /*public void printSource(CSource cs){
        cs.header=this;
        cs.print();
    }*/
    
    public String getInclude(){
        return rpath;
    }

    
    
    
}
