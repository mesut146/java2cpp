package com.mesut.j2cpp;

import com.mesut.j2cpp.ast.*;
import com.mesut.j2cpp.map.ClassMap;
import com.mesut.j2cpp.util.ForwardDeclarator;
import com.mesut.j2cpp.util.TypeHelper;
import com.mesut.j2cpp.visitor.TypeVisitor;
import org.eclipse.jdt.core.dom.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LibImplHandler {
    public static LibImplHandler instance = new LibImplHandler();

    ClassMap classMap = new ClassMap();
    TypeVisitor typeVisitor;
    CHeader forwardHeader;
    CHeader allHeader;

    public LibImplHandler() {
        forwardHeader = new CHeader("lib_common.h");
        allHeader = new CHeader("lib_all.h");
        typeVisitor = new TypeVisitor(allHeader);
    }

    CType makeType(ITypeBinding binding) {
        return typeVisitor.fromBinding(binding);
    }

    CClass getClazz(ITypeBinding binding) {
        CType type = makeType(binding);
        CClass cc = classMap.get(type);
        if (cc.base.isEmpty()) {
            if (binding.getSuperclass() != null) {
                CType superCls = makeType(binding.getSuperclass());
                classMap.get(superCls);
                if (Config.baseClassObject || !superCls.equals(TypeHelper.getObjectType())) {
                    cc.base.add(superCls);
                }
            }
            for (ITypeBinding iface : binding.getInterfaces()) {
                CType it = makeType(iface);
                classMap.get(it);
                cc.base.add(it);
            }
        }
        return cc;
    }

    public void addMethod(IMethodBinding binding) {
        ITypeBinding real = binding.getDeclaringClass().getErasure();//for generic types
        CClass clazz = getClazz(real);

        //get real method
        for (IMethodBinding methodBinding : real.getDeclaredMethods()) {
            if (!methodBinding.getName().equals(binding.getName())) continue;
            if (binding.isSubsignature(methodBinding)) {
                binding = methodBinding;
                break;
            }
        }
        List<CType> params = new ArrayList<>();
        for (ITypeBinding p : binding.getParameterTypes()) {
            params.add(makeType(p));
        }
        CMethod method;
        //method = null;
        method = findMethod(clazz, binding.getName(), params);
        if (method != null) return;

        method = new CMethod();
        method.name = CName.from(binding.getName());
        method.type = makeType(binding.getReturnType());
        method.isCons = binding.isConstructor();
        method.isPureVirtual = Modifier.isAbstract(binding.getModifiers());
        for (CType ptype : params) {
            method.params.add(new CParameter(ptype, CName.from("p")));
        }
        clazz.addMethod(method);
    }

    CMethod findMethod(CClass cc, String name, List<CType> params) {
        for (CMethod method : cc.methods) {
            if (!method.name.is(name) || method.params.size() != params.size()) {
                continue;
            }
            boolean found = true;
            for (int i = 0; i < method.params.size(); i++) {
                CParameter cp = method.params.get(i);
                if (!cp.type.equals(params.get(i))) {
                    found = false;
                    break;
                }
            }
            if (found) return method;
        }
        return null;
    }

    public void addField(IVariableBinding binding) {
        if (binding.getDeclaringClass() == null) {
            return;
        }
        if (!binding.isField() || !binding.isEnumConstant() || binding.getDeclaringClass().isFromSource()) {
            return;
        }
        CClass decl = getClazz(binding.getDeclaringClass().getErasure());
        for (CField field : decl.fields) {
            if (field.name.name.equals(binding.getName())) {
                return;
            }
        }

        CField field = new CField();
        field.setName(CName.from(binding.getName()));
        field.setType(makeType(binding.getType()));

        decl.addField(field);
    }

    public void addType(CType type) {

    }

    public void writeAll(File dir) {
        forwardHeader.forwardDeclarator = new ForwardDeclarator(classMap);
        for (CClass cc : classMap.map.values()) {
            CHeader header = new CHeader(cc.getType().basicForm().replace("::", "/") + ".h");
            header.ns = cc.getType().ns;
            header.addClass(cc);

            try {
                Util.writeHeader(header, dir);
            } catch (IOException e) {
                e.printStackTrace();
            }
            allHeader.addInclude(cc.getType());
            forwardHeader.forwardDeclarator.add(cc);
        }
        allHeader.includes.add(0, forwardHeader.getInclude());
        try {
            Util.writeHeader(forwardHeader, dir);
            Collections.sort(allHeader.includes);
            Util.writeHeader(allHeader, dir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
