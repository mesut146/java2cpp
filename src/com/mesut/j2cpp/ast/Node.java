package com.mesut.j2cpp.ast;
import java.io.*;
import java.util.*;

public abstract class Node
{
    public String indention="";
    public boolean useTab=false;
    public int level=0;
    public ByteArrayOutputStream baos=new ByteArrayOutputStream();
    public List<String> list=new ArrayList<>();
    boolean isPrinted=false;
    
    public abstract void print();
    
    void init(){
        indention="";
        String str=getIndent();
        for(int i=0;i<level;i++){
            indention=indention+str;
        }
    }
    
    String getIndent(){
        return useTab?"\t":"    ";
    }
    
    public Node line(String str){
        //write(indention).write(indention).write(str);
        list.add(str);
        return this;
    }
    public Node lineln(String str){
        //line(str).println();
        line(str);
        println();
        return this;
    }
    public Node lineup(String s){
        line(getIndent());
        line(s);
        return this;
    }
    public void println(){
        //write("\n");
        list.add("\n");
    }
    public Node append(String str){
        //write(indention).write(str);
        if(list.size()==0){
            list.add(str);
        }else{
            int idx=list.size()-1;
            list.set(idx,list.get(idx)+str);
        }
        return this;
    }
    public Node append(Node n){
        n.print();
        for(String s:n.list){
            list.add(s);
        }
        return this;
    }
    /*
    public Node appendln(String str){
        append(str).println();
        return this;
    }*/
    
    public Node write(String str){
        try
        {
            baos.write(str.getBytes());
            baos.flush();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return this;
    }
    
    public void upTo(Node n){
        n.level=this.level+1;
        n.init();
    }
    public void up(){
        level++;
        init();
    }

    @Override
    public String toString()
    {
        /*if(!isPrinted){
            print();
            isPrinted=true;
        }*/
        //baos.reset();
        print();
        StringBuilder sb=new StringBuilder();
        for(String s:list){
            sb.append(s);
        }
        return sb.toString();
    }
    
    
}
