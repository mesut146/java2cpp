package com.mesut.j2cpp.ast;
import com.mesut.j2cpp.*;

public class CField extends HasModifier
{
    public TypeName type;
    public String name,right;
    
    
    public void print(){
        if(isStatic()){
            append("static ");
        }
        append(type.toString());
        if(isPointer()&&!type.isArray()){
            append("*");
        }
        append(" ");
        append(name);
        if(right!=null){
            append("=");
            append(right);
        }
        append(";");
        //TODO right
    }



    boolean isPointer(){
        return !Helper.is(type.toString());
    }
}
