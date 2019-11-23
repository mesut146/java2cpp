package com.mesut.j2cpp.ast;
import java.io.*;

public abstract class Node
{
    public String indention="";
    public boolean useTab=false;
    public int level=0;
    public ByteArrayOutputStream baos=new ByteArrayOutputStream();
    boolean isPrinted=false;
    
    public abstract void print();
    
    void init(){
        indention="";
        String str=useTab?"\t":"    ";
        for(int i=0;i<level;i++){
            indention=indention+str;
        }
    }
    
    //indention level
    public Node line(String str){
        write(indention).write(indention).write(str);
        return this;
    }
    public Node lineln(String str){
        line(str).println();
        return this;
    }
    public void println(){
        write("\n");
    }
    public Node append(String str){
        write(indention).write(str);
        return this;
    }
    public Node appendln(String str){
        append(str).println();
        return this;
    }
    
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
        return baos.toString();
    }
    
    
}
