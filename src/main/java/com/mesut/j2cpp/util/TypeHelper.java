package com.mesut.j2cpp.util;

import com.mesut.j2cpp.Config;
import com.mesut.j2cpp.ast.CType;
import com.mesut.j2cpp.ast.Namespace;

import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;

public class TypeHelper {

    static HashMap<String, String> prims = new HashMap<>();

    static {
        prims.put("byte", "char");
        prims.put("char", "wchar_t");
        prims.put("short", "char16_t");
        prims.put("float", "float");
        prims.put("int", "int");
        prims.put("double", "double");
        prims.put("long", "long");
        prims.put("boolean", "bool");
    }

    public static boolean isPrim(String ty) {
        return prims.containsValue(ty);
    }

    public static String toCType(String ty) {
        if (prims.containsKey(ty)) {
            return prims.get(ty);
        }
        return ty;
    }


    public static CType getStringType() {
        return new CType("java::lang::String");
    }

    public static CType getObjectType() {
        return new CType("java::lang::Object");
    }

    public static CType getClassType() {
        return new CType("java::lang::Class");
    }

    public static CType getVectorType() {
        return new CType("std::vector");
    }

    public static CType getEnumType() {
        return new CType("java::lang::Enum");
    }

    public static CType getVoidType() {
        return new CType("void");
    }

    public static CType getAutoType() {
        return new CType("auto");
    }


    //trim type's namespace by usings
    //java::lang::String   using java::lang -> String
    public static CType normalizeType(CType type, List<Namespace> usings) {
        if (type.ns == null) {
            return type;
        }
        CType copied = type.copy();
        Namespace typeNs = copied.ns;
        //trim matching using's ns
        for (Namespace ns : usings) {
            if (ns.getAll().startsWith(typeNs.getAll())) {
                String str = ns.getAll().substring(typeNs.getAll().length());
                if (str.startsWith("::")) {
                    str = str.substring(2);
                }
                if (str.isEmpty()) {
                    copied.ns = null;
                }
                else {
                    copied.ns = new Namespace(str);
                }
                return copied;
            }
        }
        return copied;
    }

}
