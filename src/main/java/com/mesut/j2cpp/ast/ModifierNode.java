package com.mesut.j2cpp.ast;

import java.util.HashSet;
import java.util.Set;

public abstract class ModifierNode extends Node {

    final static String PUBLIC_NAME = "public";
    final static String PRIVATE_NAME = "private";
    final static String PROTECTED_NAME = "protected";
    final static String STATIC_NAME = "static";
    final static String VIRTUAL_NAME = "virtual";
    final static String NATIVE_NAME = "native";
    final static String EXTERN_NAME = "extern";
    Set<String> modifiers = new HashSet<>();

    public boolean isPublic() {
        return modifiers.contains(PUBLIC_NAME);
    }

    public void setPublic(boolean b) {
        if (b) {
            modifiers.add(PUBLIC_NAME);
        }
        else {
            modifiers.remove(PUBLIC_NAME);
        }
    }

    public boolean isPrivate() {
        return !isPublic();
    }

    public void setPrivate(boolean b) {
        setPublic(!b);
        if (b) {
            modifiers.add(PRIVATE_NAME);
        }
        else {
            modifiers.remove(PRIVATE_NAME);
        }
    }

    public boolean isProtected() {
        return modifiers.contains(PROTECTED_NAME);
    }

    public void setProtected(boolean b) {
        setPublic(!b);
        if (b) {
            modifiers.add(PROTECTED_NAME);
        }
        else {
            modifiers.remove(PROTECTED_NAME);
        }
    }

    public boolean isStatic() {
        return modifiers.contains(STATIC_NAME);
    }

    public void setStatic(boolean b) {
        if (b) {
            modifiers.add(STATIC_NAME);
        }
        else {
            modifiers.remove(STATIC_NAME);
        }
    }

    public boolean isNative() {
        return modifiers.contains(NATIVE_NAME);
    }


    public void setNative(boolean b) {
        if (b) {
            modifiers.add(NATIVE_NAME);
        }
        else {
            modifiers.remove(NATIVE_NAME);
        }
    }

    public boolean isVirtual() {
        return modifiers.contains(VIRTUAL_NAME);
    }


    public void setVirtual(boolean b) {
        if (b) {
            modifiers.add(VIRTUAL_NAME);
        }
        else {
            modifiers.remove(VIRTUAL_NAME);
        }
    }

    public boolean isExtern() {
        return modifiers.contains(EXTERN_NAME);
    }


    public void setExtern(boolean b) {
        if (b) {
            modifiers.add(EXTERN_NAME);
        }
        else {
            modifiers.remove(EXTERN_NAME);
        }
    }
}
