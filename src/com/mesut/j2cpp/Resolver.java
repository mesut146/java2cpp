package com.mesut.j2cpp;

import java.util.ArrayList;
import java.util.List;

public class Resolver {
    String pkgStr;
    String[] pkgArr;
    static String srcPath;
    public static List<String> jreFiles=new ArrayList<>();
    public static List<String> projectFiles=new ArrayList<>();

    public static String resolve(String fullname){
        return null;

    }

    public static boolean isClass(String name,CHeader header){
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
        return false;
    }

    public static void init(){

    }
}
