package com.mesut.j2cpp;

import com.mesut.j2cpp.ast.CClass;
import com.mesut.j2cpp.ast.CSource;
import com.mesut.j2cpp.ast.CType;
import com.mesut.j2cpp.map.ClassMap;
import com.mesut.j2cpp.util.BaseClassSorter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class IncludeHelper {

    //include all referenced headers
    public static void handle(CSource source) {
        Set<CClass> set = new HashSet<>();
        for (CClass cc : source.classes) {
            collect(cc, set);
        }
        //remove from include list
        for (CClass cc : set) {
            source.includes.remove(new IncludeStmt(cc.getHeaderPath()));
            for (IncludeStmt includeStmt : cc.includes.list) {
                source.addInclude(includeStmt);
            }
        }
        List<CClass> list = new ArrayList<>(set);
        try {
            BaseClassSorter.sort(list);
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (CClass type : list) {
            source.addInclude(new IncludeStmt(type.getHeaderPath()));
        }

    }

    //recursively collect types
    static void collect(CClass cc, Set<CClass> list) {
        if (cc == null || cc.isAnonymous) return;
        if (!list.add(cc)) {
            return;
        }
        if (cc.superClass != null) {
            collect(ClassMap.sourceMap.get(cc.superClass), list);
        }
        for (CType iface : cc.ifaces) {
            collect(ClassMap.sourceMap.get(iface), list);
        }
        for (CType type : cc.types) {
            CClass c = ClassMap.sourceMap.get(type);
            collect(c, list);
        }
    }
}
