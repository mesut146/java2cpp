package com.mesut.j2cpp;
import com.mesut.j2cpp.ast.*;
import java.util.*;
import java.io.*;

public class CHeader extends Node
{
    public String name;
    public List<String> includes=new ArrayList<>();
    public List<CClass> classes=new ArrayList<>();
    public Namespace ns;
    public String rpath;
    //public List<CMethod> methods=new ArrayList<>();
    
    
    public void addClass(CClass cc){
        //cc.inHeader=true;
        cc.ns=ns;
        classes.add(cc);
    }

    public void print()
    {
        append("#pragma once");
        println();
        println();
        for(String imp:includes){
            append("#include \"");
            append(imp).append("\"").println();
        }
        println();
        append("using namespace ").append(ns.all).append(";");
        println();
        for(CClass cc:classes){
            cc.forHeader=true;
            append(cc);
        }
    }
    
    public void printSource(CSource cs){
        cs.h=this;
        cs.print();
    }
    
    public String getInclude(){
        return rpath;
    }

    
    
    
}
