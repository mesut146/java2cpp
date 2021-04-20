package com.mesut.j2cpp.map;

import com.mesut.j2cpp.Config;
import com.mesut.j2cpp.ast.CClass;
import com.mesut.j2cpp.ast.CField;
import com.mesut.j2cpp.ast.CMethod;
import com.mesut.j2cpp.ast.CType;
import com.mesut.j2cpp.util.TypeHelper;
import com.mesut.j2cpp.visitor.PreVisitor;
import com.mesut.j2cpp.visitor.TypeVisitor;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

//hold all classes
//needed for inheritance relation
public class ClassMap {
    public static ClassMap sourceMap = new ClassMap();
    public Map<CType, CClass> map = new HashMap<>();
    public CClass mainClass;

    public static CMethod getAddedMethod(CClass cc, IMethodBinding binding, List<CType> params, CType type) {
        String name = binding.getMethodDeclaration().getName();
        for (CMethod method : cc.methods) {
            if (!method.name.is(name) || method.params.size() != params.size()) {
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
        if (Config.mainClass != null && type.basicForm().equals(mainClass.getType().basicForm())) {
            return mainClass;
        }
        if (type.mapped || type.equals(TypeHelper.getVectorType()) || type.isPrim() || type.isVoid()) return null;
        if (map.containsKey(type)) {
            return map.get(type);
        }
        CClass cc = new CClass(type);
        map.put(type, cc);
        return cc;
    }

    public CClass get(ITypeBinding binding) {
        return get(TypeVisitor.fromBinding(binding));
    }

    public CClass init(ITypeBinding binding) {
        binding = binding.getErasure();
        CClass cc = get(binding);
        if (cc == null) return null;
        PreVisitor.initType(binding, cc, null);
        return cc;
    }

    public CMethod getMethod(IMethodBinding binding) {
        CClass cc = get(TypeVisitor.fromBinding(binding.getDeclaringClass()));
        return PreVisitor.visitMethod(binding, cc);
    }

}
