package com.mesut.j2cpp.ast;

import com.mesut.j2cpp.Helper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CType {
    public Namespace ns;
    public String type;//e.g String,Object
    public int arrayLevel = 0;
    public List<CType> typeNames = new ArrayList<>();
    public CType scope = null;

    public CType(String type) {
        String[] arr = type.split("::");
        this.type = arr[arr.length - 1];
        if (arr.length > 1) {
            ns = new Namespace(type.substring(0,type.lastIndexOf("::")));
        }
        //this.type = type;
    }

    public String getName(){
        return type;
    }

    public String full() {
        if (ns == null) {
            return type;
        }
        return ns.all + "::" + type;
    }

    public boolean isArray() {
        return arrayLevel > 0;
    }

    public boolean isPrim() {
        return Helper.isPrim(type);
    }

    public boolean isPointer() {
        return !isVoid() && (!isPrim() || isArray());
    }

    public boolean isVoid() {
        return type.equals("void");
    }

    @Override
    public String toString() {
        if (isArray()) {
            return strLevel(arrayLevel);
        }
        if (typeNames.size() > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append(type);
            sb.append("<");
            for (Iterator<CType> iterator = typeNames.iterator(); iterator.hasNext(); ) {
                sb.append(iterator.next().full());
                if (iterator.hasNext()) {
                    sb.append(",");
                }
            }
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

}
