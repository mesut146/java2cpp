package com.mesut.j2cpp;

import com.mesut.j2cpp.ast.CClass;
import com.mesut.j2cpp.ast.CSource;
import com.mesut.j2cpp.ast.CType;
import com.mesut.j2cpp.map.ClassMap;
import com.mesut.j2cpp.visitor.TypeVisitor;
import org.eclipse.jdt.core.dom.ITypeBinding;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class IncludeHelper {

    //include all referenced headers
    public static void handle(CSource source) {
        Set<CType> list = new HashSet<>();
        for (CClass cc : source.classes) {
            handle(cc, list);
        }
        for (CType type : list) {
            source.addInclude(type);
        }
        //todo sort
    }

    static void handle(CClass cc, Set<CType> list) {
        for (CType type : cc.types) {
            if (list.add(type)) {
                handle(ClassMap.sourceMap.get(type), list);//recurse
            }
        }
    }

    public static List<CType> collect(TypeVisitor visitor, ITypeBinding binding) {
        List<CType> list = new ArrayList<>();
        if (binding == null) {
            return list;
        }
        if (binding.getSuperclass() != null) {
            list.add(visitor.fromBinding(binding.getSuperclass()));
            collect(visitor, binding.getSuperclass());
        }
        for (ITypeBinding iface : binding.getInterfaces()) {
            list.add(visitor.fromBinding(iface));
            collect(visitor, iface);
        }
        return list;
    }
}
