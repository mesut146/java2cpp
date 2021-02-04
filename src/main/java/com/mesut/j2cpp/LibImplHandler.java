package com.mesut.j2cpp;

import com.mesut.j2cpp.ast.*;
import com.mesut.j2cpp.map.ClassMap;
import com.mesut.j2cpp.util.ForwardDeclarator;
import com.mesut.j2cpp.util.TypeHelper;
import com.mesut.j2cpp.visitor.TypeVisitor;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Modifier;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

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
        CType type = typeVisitor.fromBinding(binding);
        CClass decl = classMap.get(type);
        if (decl.base.isEmpty()) {
            if (binding.getSuperclass() != null) {
                CType superCls = makeType(binding.getSuperclass());
                classMap.get(superCls);
                if (Config.baseClassObject || !superCls.equals(TypeHelper.getObjectType())) {
                    decl.base.add(superCls);
                }
            }
            for (ITypeBinding iface : binding.getInterfaces()) {
                CType it = makeType(iface);
                classMap.get(it);
                decl.base.add(it);
            }
        }
        return decl;
    }

    public void addMethod(IMethodBinding binding) {
        CClass decl = getClazz(binding.getDeclaringClass().getErasure());
        for (CMethod method : decl.methods) {
            if (method.name.name.equals(binding.getName())) {
                return;
            }
        }
        //System.out.println(binding.getDeclaringClass().getQualifiedName() + "." + binding.getName() + "()");
        CMethod method = new CMethod();
        method.name = CName.from(binding.getName());
        method.type = makeType(binding.getReturnType());
        method.isCons = binding.isConstructor();
        method.isPureVirtual = Modifier.isAbstract(binding.getModifiers());
        for (ITypeBinding p : binding.getParameterTypes()) {
            method.params.add(new CParameter(makeType(p.getErasure()), CName.from("p")));
        }
        decl.addMethod(method);
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
