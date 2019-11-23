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
        cc.inHeader=true;
        classes.add(cc);
    }

    public void print()
    {
        append("#pragma once\n\n");
        for(String imp:includes){
            append("#include \"");
            append(imp).appendln("\"");
        }
        for(CClass cc:classes){
            cc.ns=ns;
            append(cc.toString());
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
