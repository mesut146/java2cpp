package com.mesut.j2cpp.map;

import com.mesut.j2cpp.ast.*;
import com.mesut.j2cpp.visitor.PreVisitor;
import com.mesut.j2cpp.visitor.TypeVisitor;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

//hold all classes
//needed for inheritance relation
public class ClassMap {
    public static ClassMap sourceMap = new ClassMap();
    public Map<CType, CClass> map = new HashMap<>();

    public static CMethod getAddedMethod(CClass cc, IMethodBinding binding, List<CType> params, CType type) {
        for (CMethod method : cc.methods) {
            if (!method.name.is(binding.getName()) || method.params.size() != params.size() || !type.equals(method.type)) {
                continue;
            }
            boolean found = true;
            for (int i = 0; i < params.size(); i++) {
                if (!params.get(i).equals(method.params.get(i).type)) {
                    found = false;
                    break;
                }
            }
            if (found) return method;
        }
        return null;
    }

    public static CField getAddedField(CClass cc, IVariableBinding binding, CType type) {
        for (CField field : cc.fields) {
            if (field.type.equals(type) && field.name.is(binding.getName())) {
                return field;
            }
        }
        return null;
    }

    public CClass get(CType type) {
        if (map.containsKey(type)) {
            return map.get(type);
        }
        CClass cc = new CClass(type);
        map.put(type, cc);
        return cc;
    }

    public CMethod getMethod(IMethodBinding binding) {
        CClass cc = get(TypeVisitor.fromBinding(binding.getDeclaringClass()));
        return PreVisitor.visitMethod(binding, cc);
    }

}
