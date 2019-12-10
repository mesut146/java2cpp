package com.mesut.j2cpp.ast;

import java.util.HashSet;
import java.util.Set;

public abstract class HasModifier extends Node{
    Set<String> modifiers = new HashSet<>();

    public boolean isPublic(){
        return modifiers.contains("public");
    }

    public boolean isPrivate(){
        return !isPublic();
    }

    public boolean isStatic() {
        return modifiers.contains("static");
    }

    public void setPublic(boolean b){
        if (b){
            modifiers.add("public");
        }else{
            modifiers.remove("public");
        }
    }

    public void setStatic(boolean b){
        if (b){
            modifiers.add("static");
        }else{
            modifiers.remove("static");
        }
    }

    public void setNative(boolean b){
        if (b){
            modifiers.add("native");
        }else{
            modifiers.remove("native");
        }
    }
}
