package com.mesut.j2cpp;

import java.util.ArrayList;
import java.util.List;

//represents single package like java/lang,java.io
class PackageName{
    List<String> names=new ArrayList<>();

    public PackageName(String name) {
        //java/lang
        String[] arr=name.split("/");
        for (String sub:arr){
            names.add(sub);
        }
    }

    //is prefix sub name of this package e.g java->java/lang , java.util->java.util.regex
    public boolean has(String prefix){
        PackageName target=new PackageName(prefix);
        for (String name:names){

        }
        return false;
    }
}
