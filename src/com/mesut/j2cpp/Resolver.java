package com.mesut.j2cpp;

import com.mesut.j2cpp.ast.CHeader;

import java.util.ArrayList;
import java.util.List;

public class Resolver {
    String pkgStr;
    String[] pkgArr;
    SymbolTable table;

    public static List<String> jreFiles=new ArrayList<>();
    public static List<String> projectFiles=new ArrayList<>();

    public Resolver(SymbolTable table) {
        this.table = table;
    }

    public static String resolve(String fullname){
        return null;

    }

    public boolean isClass(String name, CHeader header){
        for(String str:header.includes){
            if (str.endsWith(".h")){
                int idx=str.lastIndexOf('/');
                if (idx!=-1){
                    String cls=str.substring(idx+1,str.length()-2);
                    if (name.equals(cls)){
                        return true;
                    }
                }
            }
        }
        //TODO: check same package too
        return false;
    }

    public static void init(){

    }
}
