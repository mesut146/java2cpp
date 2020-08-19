package com.mesut.j2cpp;

import com.mesut.j2cpp.ast.CType;

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

    public static CType getEnumType() {
        return new CType("java::lang::Enum");
    }

}
