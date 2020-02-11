package com.mesut.j2cpp;

import com.mesut.j2cpp.ast.CHeader;
import com.mesut.j2cpp.ast.CType;

import java.util.ArrayList;
import java.util.List;

public class Resolver {
    String pkgStr;
    String[] pkgArr;
    SymbolTable table;
    Converter converter;

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
            int idx = str.lastIndexOf('/');
            if (idx != -1) {
                String cls = str.substring(idx + 1);
                if (name.equals(cls)) {
                    return true;
                }
            }
        }
        //TODO: check same package too
        return false;
    }

    public CType resolveType(String typeStr, CHeader header) {
        String[] arr=typeStr.split(".");
        if(arr.length>1){
            //todo fulltype
            // java.lang.String  com.Base.inner
        }
        //include java/lang/Class.h
        for (String include : header.includes) {
            int idx = include.lastIndexOf('/');
            if (idx != -1) {
                //get class name
                String cls = include.substring(idx + 1);
                if (typeStr.equals(cls)) {//directly imported
                    return toType(include);
                }
            }
        }
        for(String imp:header.importStar){
            // imp=java/lang   type=String
            String[] arr2=imp.split("/");
            PackageNode pn=getHier(arr2);
            if(pn!=null){
                return toType(pn,);
            }  
        }
        return null;
    }
    
    PackageNode getHier(String[] arr){
        for(PackageNode pn:converter.packageHierarchy){
            if(pn.has(arr,0)){
                return pn;
            }
        }
        return null;
    }
    
    CType toType(PackageNode node){
        
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
