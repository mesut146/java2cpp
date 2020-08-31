package com.mesut.j2cpp.util;

import com.mesut.j2cpp.ast.CType;
import com.mesut.j2cpp.ast.Namespace;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Helper {

    static List<String> java_prims = Arrays.asList("byte", "char", "short", "float", "int", "double", "long", "boolean");
    static List<String> c_prims = Arrays.asList("char", "char16_t", "float", "int", "double", "long", "bool");
    static HashMap<String, String> prims = new HashMap<String, String>() {{
        put("byte", "char");
        put("char", "char");
        put("short", "char16_t");
        put("float", "float");
        put("int", "int");
        put("double", "double");
        put("long", "long");
        put("boolean", "bool");
    }};

    public static boolean isPrim(String ty) {
        return c_prims.contains(ty);
    }

    public static String toCType(String ty) {
        if (java_prims.contains(ty)) {
            return prims.get(ty);
        }
        return ty;
    }


    public static CType getStringType() {
        return new CType("java::lang::String");
    }

    public static CType getVectorType() {
        return new CType("std::vector");
    }

    public static CType getEnumType() {
        return new CType("java::lang::Enum");
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
        //find longest prefix
        CType copied = type.copy();
        int max = 0;
        Namespace maxNs = null;
        Namespace typeNs = copied.ns;
        for (Namespace ns : usings) {
            int cur = 0;
            for (int i = 0; i < Math.min(ns.parts.size(), typeNs.parts.size()); i++) {
                if (typeNs.parts.get(i).equals(ns.parts.get(i))) {
                    cur++;
                }
                else {
                    break;
                }
            }
            if (cur > max) {
                maxNs = ns;
                max = cur;
            }
        }
        if (maxNs != null) {
            String res = typeNs.normalize(maxNs);
            if (res.isEmpty()) {
                copied.ns = null;
            }
            else {
                copied.ns = new Namespace(res);
            }
        }
        return copied;
    }

}
