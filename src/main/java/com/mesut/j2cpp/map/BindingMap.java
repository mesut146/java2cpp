package com.mesut.j2cpp.map;

import com.mesut.j2cpp.ast.CType;
import com.mesut.j2cpp.visitor.TypeVisitor;
import org.eclipse.jdt.core.dom.ITypeBinding;

import java.util.HashMap;
import java.util.Map;

public class BindingMap {
    public static Map<CType, ITypeBinding> map = new HashMap<>();

    public static void add(CType type, ITypeBinding binding) {
        if (!map.containsKey(type)) {
            map.put(type, binding);
        }
    }

    public static void add(ITypeBinding binding) {
        add(TypeVisitor.fromBinding(binding), binding);
    }

    public static ITypeBinding get(CType type) {
        return map.get(type);
    }
}
