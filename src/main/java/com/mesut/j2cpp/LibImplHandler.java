package com.mesut.j2cpp;

import com.mesut.j2cpp.ast.*;
import com.mesut.j2cpp.map.ClassMap;
import com.mesut.j2cpp.util.ForwardDeclarator;
import com.mesut.j2cpp.util.TypeHelper;
import com.mesut.j2cpp.visitor.TypeVisitor;
import org.eclipse.jdt.core.dom.*;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

public class LibImplHandler {
    public static LibImplHandler instance = new LibImplHandler();

    ClassMap classMap = new ClassMap();
    CHeader forwardHeader;
    CHeader allHeader;

    public LibImplHandler() {
        forwardHeader = new CHeader("lib_common.h");
        allHeader = new CHeader("lib_all.h");
    }

    CClass getClazz(ITypeBinding binding) {
        CType type = TypeVisitor.fromBinding(binding);
        CClass cc = classMap.get(type);
        if (cc.superClass == null) {
            if (binding.getSuperclass() != null) {
                CType superCls = TypeVisitor.fromBinding(binding.getSuperclass(), cc);
                classMap.get(superCls);
                if (Config.baseClassObject || !superCls.equals(TypeHelper.getObjectType())) {
                    cc.setSuper(superCls);
                }
            }
        }
        if (cc.ifaces.isEmpty()) {
            for (ITypeBinding iface : binding.getInterfaces()) {
                CType it = TypeVisitor.fromBinding(iface, cc);
                classMap.get(it);
                cc.addBase(it);
            }
        }
        return cc;
    }

    public void addMethod(IMethodBinding binding) {
        ITypeBinding real = binding.getDeclaringClass().getErasure();//for generic types

        //get real method
        for (IMethodBinding methodBinding : real.getDeclaredMethods()) {
            if (!methodBinding.getName().equals(binding.getName())) continue;
            if (binding.isSubsignature(methodBinding)) {
                binding = methodBinding;
                break;
            }
        }
        classMap.getMethod(binding);
    }

    public void addField(IVariableBinding binding) {
        if (binding.getDeclaringClass() == null) {
            return;
        }
        if (!binding.isField() || !binding.isEnumConstant() || binding.getDeclaringClass().isFromSource()) {
            return;
        }
        CClass cc = getClazz(binding.getDeclaringClass().getErasure());
        for (CField field : cc.fields) {
            if (field.name.name.equals(binding.getName())) {
                return;
            }
        }
        CField field = new CField();
        field.setName(CName.from(binding.getName()));
        field.setType(TypeVisitor.fromBinding(binding.getType(), cc));

        cc.addField(field);
    }

    public void writeAll(File dir, CHeader common) {
        forwardHeader.forwardDeclarator = new ForwardDeclarator(classMap);
        if (Config.include_common_forwards) {
            forwardHeader.addInclude(common.getInclude());
        }
        for (CClass cc : classMap.map.values()) {
            CHeader header = new CHeader(cc.getType().basicForm().replace("::", "/") + ".h");
            header.setNs(cc.getType().ns);
            header.setClass(cc);
            try {
                Util.writeHeader(header, dir);
            } catch (IOException e) {
                e.printStackTrace();
            }
            allHeader.addInclude(cc.getType());
            forwardHeader.forwardDeclarator.add(cc);
        }
        allHeader.addInclude(0, forwardHeader.getInclude());
        try {
            Util.writeHeader(forwardHeader, dir);
            Util.writeHeader(allHeader, dir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
