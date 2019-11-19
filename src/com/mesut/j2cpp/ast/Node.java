package com.mesut.j2cpp.ast;
import java.io.*;

public abstract class Node
{
    public String indention="";
    public ByteArrayOutputStream baos=new ByteArrayOutputStream();
    public PrintWriter pw=new PrintWriter(baos);;
    
    /*public Node(PrintWriter p){
        pw=p;
    }*/
    
    public abstract void print();
        
    
    public void print(String str){
        pw.append(indention).print(str);
    }
    public void println(String str){
        pw.append(indention).println(str);
    }
    public void println(){
        pw.println();
    }
    public Node append(String str){
        pw.print(str);
        return this;
    }
    
    void up(Node n){
        n.indention=indention+"    ";
    }
    void up(){
        indention+="    ";
    }
    void tab(PrintWriter pw){
        pw.print("    ");
    }

    @Override
    public String toString()
    {
        return baos.toString();
    }
    
    
}
