package com.mesut.j2cpp.util;

import com.mesut.j2cpp.ast.CType;
import org.eclipse.jdt.core.dom.ITypeBinding;

import java.util.HashMap;
import java.util.Map;

public class BindingMap {
    public static Map<CType, ITypeBinding> map = new HashMap<>();

    public static void add(CType cType, ITypeBinding binding) {
        map.put(cType, binding);
    }

    public static ITypeBinding get(CType type) {
        return map.get(type);
    }
}
