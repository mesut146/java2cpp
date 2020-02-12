package com.mesut.j2cpp.ast;

import com.mesut.j2cpp.Helper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CType {
    public Namespace ns;
    public String type;//e.g String,Object
    public int arrayLevel = 0;
    public List<CType> typeNames = new ArrayList<>();
    public CType scope = null;
    public boolean isTemplate = false;
    public boolean pointer = true;

    public CType(String type) {
        String[] arr = type.split("::");
        this.type = arr[arr.length - 1];
        if (arr.length > 1) {
            ns = new Namespace(type.substring(0, type.lastIndexOf("::")));
        }
        //this.type = type;
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

    public boolean isArray() {
        return arrayLevel > 0;
    }

    public boolean isPrim() {
        return Helper.isPrim(type);
    }

    public boolean isPointer() {
        //return !isVoid() && (!isPrim() || isArray());
        return !isVoid() && !isPrim() && !isTemplate && pointer;
    }

    public boolean isVoid() {
        return type.equals("void");
    }

    /*public String toPointerString(){

    }*/

    @Override
    public String toString() {
        if (isArray()) {
            return strLevel(arrayLevel);
        }
        if (typeNames.size() > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append(type);
            sb.append("<");
            /*for (Iterator<CType> iterator = typeNames.iterator(); iterator.hasNext(); ) {
                sb.append(iterator.next().full());
                if (iterator.hasNext()) {
                    sb.append(",");
                }
            }*/
            sb.append(typeNames.stream().map(CType::full).collect(Collectors.joining(",")));
            sb.append(">");
            return sb.toString();
        }
        return full();
    }

    String strLevel(int level) {
        if (level == 0) {
            return full();
        } else if (level == 1) {
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
