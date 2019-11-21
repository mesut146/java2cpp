package com.mesut.j2cpp.ast;
import java.util.*;
import java.io.*;

public class CClass extends Node
{
    public String name;
    public List<String> base=new ArrayList<>();
    public List<CField> fields=new ArrayList<>();
    public List<CMethod> methods=new ArrayList<>();
    public List<CClass> classes=new ArrayList<>();
    public boolean isInterface=false;
    public CClass parent;
    public Namespace ns=null;
    public boolean inHeader=false;
    
    public void addInner(CClass cc){
        cc.parent=this;
        cc.inHeader=inHeader;
        classes.add(cc);
    }
    public void addMethod(CMethod cm){
        cm.parent=this;
        methods.add(cm);
    }
    public void addField(CField cf){
        fields.add(cf);
    }
    
    public Namespace getNamespace(){
        String str;
        if(parent==null){//header level
            return ns;
        }
        str=parent.getNamespace().all+"::"+name;
        Namespace n=new Namespace();
        n.all=(str);
        return n;
    }
    
    public Namespace getNamespaceFull(){
        String str;
        Namespace n=new Namespace();
        if(parent!=null){
            str=parent.getNamespace().all+"::";
            n.pkg(str);
        }
        else{
            return ns;
        }
        return n;
    }
    
    public void print()
    {
        if(parent==null&&ns!=null){
            line(ns.all);
            appendln("{");
            up();
        }
        line("class ");
        append(name);
        if(base.size()>0){
            append(":");
            for(int i=0;i<base.size();i++){
                append(base.get(i));
                if(i<base.size()-1){
                    append(",");
                }
            }
        }
        appendln("{");
        
        for(CField cf:fields){
            up(cf);
            append(cf.toString());
        }
        println();
        for(CMethod cm:methods){
            up(cm);
            append(cm.toString());
        }
        println();
        for(CClass cc:classes){
            up(cc);
            append(cc.toString());
        }
        
        lineln("};//"+name);
        
        if(parent==null&&ns!=null){
            appendln("}//ns");
        }
    }
    
    
}