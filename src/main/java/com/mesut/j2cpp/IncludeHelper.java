package com.mesut.j2cpp;

import com.mesut.j2cpp.ast.CClass;
import com.mesut.j2cpp.ast.CSource;
import com.mesut.j2cpp.ast.CType;
import com.mesut.j2cpp.visitor.TypeVisitor;
import org.eclipse.jdt.core.dom.ITypeBinding;

import java.util.ArrayList;
import java.util.List;

public class IncludeHelper {

    public static void handle(CSource source) {
        for (CClass cc : source.classes) {
            for (CType type : cc.types) {
                source.addInclude(type);
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
