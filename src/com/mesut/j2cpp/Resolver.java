package com.mesut.j2cpp;

import com.mesut.j2cpp.ast.CHeader;
import com.mesut.j2cpp.ast.CType;

import java.util.ArrayList;
import java.util.List;

public class Resolver {
    String pkgStr;
    String[] pkgArr;
    SymbolTable table;

    public static List<String> jreFiles = new ArrayList<>();
    public static List<String> projectFiles = new ArrayList<>();

    public Resolver(SymbolTable table) {
        this.table = table;
    }

    public static String resolve(String fullname) {
        return null;

    }

    //check that if class name is in import section
    public boolean isClass(String name, CHeader header) {
        for (String str : header.includes) {
            if (str.endsWith(".h")) {
                int idx = str.lastIndexOf('/');
                if (idx != -1) {
                    String cls = str.substring(idx + 1, str.length() - 2);
                    if (name.equals(cls)) {
                        return true;
                    }
                }
            }
        }
        //TODO: check same package too
        return false;
    }

    public CType resolveType(String typeStr, CHeader header) {
        //include java/lang/Class.h
        for (String include : header.includes) {
            if (include.endsWith(".h")) {//not necessary
                int idx = include.lastIndexOf('/');
                if (idx != -1) {
                    //get class name
                    String cls = include.substring(idx + 1, include.length() - 2);
                    if (typeStr.equals(cls)) {
                        return toType(include);
                    }
                }
            }
        }
        return null;
    }

    CType toType(String include) {
        CType type=new CType("");
        // java/lang/String
        // com/my/Type/Inner
        String[] arr=include.split("/");
        //todo need package hier
        return type;
    }

    public static void init() {

    }
}
