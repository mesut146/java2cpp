package com.mesut.j2cpp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//represents single package like java/lang,java.io
class PackageName {
    List<String> names = new ArrayList<>();

    public PackageName(String name) {
        //java/lang
        String[] arr = name.split("/");
        names.addAll(Arrays.asList(arr));
    }

    //is prefix sub name of this package e.g java->java/lang , java.util->java.util.regex
    public boolean isSub(String prefix) {
        PackageName target = new PackageName(prefix);
        if (names.size()<target.names.size()){
            return false;
        }
        for (int i = 0; i < target.names.size(); i++) {
            if (!target.names.get(i).equals(names.get(i))) {
                return false;
            }
        }
        return true;
    }
}
