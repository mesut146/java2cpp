package com.mesut.j2cpp;

import com.mesut.j2cpp.ast.*;
import com.mesut.j2cpp.map.ClassMap;
import com.mesut.j2cpp.util.ForwardDeclarator;
import com.mesut.j2cpp.visitor.PreVisitor;
import com.mesut.j2cpp.visitor.TypeVisitor;
import org.eclipse.jdt.core.dom.*;

import java.io.File;
import java.io.IOException;

public class LibImplHandler {
    public static LibImplHandler instance = new LibImplHandler();
    public CHeader forwardHeader;
    CHeader allHeader;

    public LibImplHandler() {
        forwardHeader = new CHeader("lib_common.h");
        allHeader = new CHeader("lib_all.h");
    }

    CClass getClazz(ITypeBinding binding) {
        CClass cc = ClassMap.sourceMap.get(binding);
        if (cc == null) return null;
        cc.fromSource = binding.isFromSource();
        PreVisitor.initType(binding, cc, null);
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
        PreVisitor.visitMethod(binding, getClazz(real));
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

    public void writeAll(File dir) {
        forwardHeader.forwardDeclarator = new ForwardDeclarator(ClassMap.sourceMap);
        for (CClass cc : ClassMap.sourceMap.map.values()) {
            if (cc.fromSource) continue;
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
            System.out.println("wrote " + forwardHeader.getInclude());
            System.out.println("wrote " + allHeader.getInclude());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
