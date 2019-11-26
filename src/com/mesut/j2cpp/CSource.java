package com.mesut.j2cpp;
import com.mesut.j2cpp.ast.*;

public class CSource extends Node
{
    CHeader h;

    @Override
    public void print()
    {
        line("#include \"");
        append(h.getInclude());
        append("\"\n").println();
        
        for(CClass cc:h.classes){
            printClass(cc);
        }
    }
    
    public void printClass(CClass cc){
        cc.inHeader=false;
        for(CMethod cm:cc.methods){
            //line("");

            append(cm);
        }
        for(CClass in:cc.classes){
            printClass(in);
        }
    }
    
    
}
