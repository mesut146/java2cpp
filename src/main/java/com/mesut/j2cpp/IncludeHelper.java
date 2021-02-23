package com.mesut.j2cpp;

import com.mesut.j2cpp.ast.CClass;
import com.mesut.j2cpp.ast.CSource;
import com.mesut.j2cpp.ast.CType;
import com.mesut.j2cpp.map.ClassMap;
import com.mesut.j2cpp.util.BaseClassSorter;
import com.mesut.j2cpp.visitor.TypeVisitor;
import org.eclipse.jdt.core.dom.ITypeBinding;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class IncludeHelper {

    //include all referenced headers
    public static void handle(CSource source) {
        Set<CClass> set = new HashSet<>();
        for (CClass cc : source.classes) {
            handle(cc, set);
            set.add(cc);
        }
        //remove from include list
        for (CClass cc : set) {
            source.includes.remove(new IncludeStmt(cc.getHeaderPath()));
        }
        List<CClass> list = new ArrayList<>(set);
        try {
            BaseClassSorter.sort(list);
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (CClass type : list) {
            source.addInclude(type.getType());
        }
    }

    //recursively collect types
    static void handle(CClass cc, Set<CClass> list) {
        for (CType type : cc.types) {
            if (type.isSys() || type.mapped) continue;
            CClass c = ClassMap.sourceMap.get(type);
            if (list.add(c)) {
                handle(c, list);//recurse
            }
        }
    }
}
