package com.mesut.j2cpp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Helper {
    static List<String> java_prims= Arrays.asList("byte","char","short","float","int","double","long","boolean");
    static List<String> java_wr=Arrays.asList("Byte","Character","Short","Float","Integer","Double","Long","Boolean");
    static HashMap<String,String> prims=new HashMap<String, String>(){{
        put("byte","char");
        put("char","char");
        put("short","char16_t");
        put("float","float");
        put("int","int");
        put("double","double");
        put("long","long");
        put("boolean","bool");
    }};
    
    static boolean is(String ty){
        return java_prims.contains(ty)||java_wr.contains(ty);
    }

    
    static String getType(String ty){
        if(java_prims.contains(ty)){
            return prims.get(ty);
        }else if(java_wr.contains(ty)){
            String pr=java_prims.get(java_wr.indexOf(ty));
            return prims.get(pr);
        }
        return null;
    }

}
