package com.mesut.j2cpp.ast;

import com.mesut.j2cpp.Helper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CType {
    public Namespace ns;
    public String type;
    public int dimensions = 0;//array dims
    public List<CType> typeNames = new ArrayList<>();//generics
    public CType scope = null;//parent type
    public boolean isTemplate = false;//<T>
    public boolean isPointer = true;//for manual cases

    public CType(String type) {
        String[] arr = type.split("::");
        this.type = arr[arr.length - 1];
        if (arr.length > 1) {
            ns = new Namespace(type.substring(0, type.lastIndexOf("::")));
        }
        //this.type = type;
    }

    public CType(String type, boolean isTemplate) {
        this(type);
        this.isTemplate = isTemplate;
    }

    public String getName() {
        return type;
    }

    //print namespace and pointer
    public String full() {
        StringBuilder sb = new StringBuilder();
        if (ns != null) {
            sb.append(ns.getAll()).append("::");
        }
        sb.append(type);
        if (isPointer()) {
            sb.append("*");
        }
        return sb.toString();
    }

    public CType copy() {
        CType copied = new CType(type, isTemplate);
        copied.isPointer = isPointer;
        return copied;
    }

    public boolean isArray() {
        return dimensions > 0;
    }

    public boolean isPrim() {
        return Helper.isPrim(type);
    }

    public boolean isPointer() {
        //return !isVoid() && (!isPrim() || isArray());
        return isPointer && !isVoid() && !isPrim() && !isTemplate;
    }

    public boolean isVoid() {
        return type.equals("void");
    }

    /*public String toPointerString(){

    }*/

    @Override
    public String toString() {
        if (isArray()) {
            return strLevel(dimensions);
        }
        if (typeNames.size() > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append(type);
            sb.append("<");
            sb.append(typeNames.stream().map(CType::full).collect(Collectors.joining(",")));
            sb.append(">");
            return sb.toString();
        }
        return full();
    }

    String strLevel(int level) {
        if (level == 0) {
            return full();
        }
        else if (level == 1) {
            return "array_single<" + strLevel(level - 1) + ">";
        }
        return "array_multi<" + strLevel(level - 1) + ">";
    }

    public String getIncludePath() {
        //todo
        StringBuilder sb = new StringBuilder();
        if (ns != null) {
            sb.append(ns.getAll().replace("::", "/"));
            sb.append("/");
        }
        sb.append(type);
        return sb.toString();
    }
}
