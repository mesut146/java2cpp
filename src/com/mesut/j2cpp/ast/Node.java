package com.mesut.j2cpp.ast;
import java.io.*;

public abstract class Node
{
    public String indention="";
    public ByteArrayOutputStream baos=new ByteArrayOutputStream();
    boolean isPrinted=false;
    
    /*public Node(PrintWriter p){
        pw=p;
    }*/
    
    public abstract void print();
        
    
    //indention level
    public Node line(String str){
        append(indention).append(str);
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
        write(str);
        return this;
    }
    public Node appendln(String str){
        append(str).println();
        return this;
    }
    
    public void write(String str){
        try
        {
            baos.write(str.getBytes());
            baos.flush();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    void up(Node n){
        n.indention=indention+"    ";
    }
    void up(){
        indention+="    ";
    }

    @Override
    public String toString()
    {
        if(!isPrinted){
            print();
            isPrinted=true;
        }
        return baos.toString();
    }
    
    
}
