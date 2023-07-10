package com.mesut.j2cpp;

import com.mesut.j2cpp.ast.CClass;
import com.mesut.j2cpp.ast.CField;
import com.mesut.j2cpp.ast.CHeader;
import com.mesut.j2cpp.ast.CName;
import com.mesut.j2cpp.map.ClassMap;
import com.mesut.j2cpp.util.ForwardDeclarator;
import com.mesut.j2cpp.visitor.PreVisitor;
import com.mesut.j2cpp.visitor.TypeVisitor;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class LibHandler {
    public static LibHandler instance = new LibHandler();
    public static boolean allMethods = true;
    public CHeader forwardHeader;

    public LibHandler() {
        forwardHeader = new CHeader("lib_common.h");
    }

    public CClass getClazz(ITypeBinding binding) {
        CClass cc = ClassMap.sourceMap.get(binding);
        if (cc == null) return null;
        PreVisitor.initType(binding, cc, null);
        return cc;
    }

    public void addType(ITypeBinding binding) {
        binding = binding.getErasure();
        CClass cc = ClassMap.sourceMap.get(binding);
        if (cc != null) {
            PreVisitor.initType(binding, cc, null);
        }
    }

    public void addMethod(IMethodBinding binding) {
        ITypeBinding real = binding.getDeclaringClass().getErasure();//for generic types

        //get real method
        binding = binding.getMethodDeclaration();
        CClass cc = getClazz(real);
        if (cc == null) return;
        if (allMethods) {
            PreVisitor.visitType(real, cc);
        }
        else {
            PreVisitor.visitMethod(binding, cc);
        }
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

    public void writeAll(Path dir) {
        forwardHeader.forwardDeclarator = new ForwardDeclarator(ClassMap.sourceMap);
        for (CClass cc : ClassMap.sourceMap.map.values()) {
            if (cc.fromSource) continue;
            CHeader header = new CHeader(cc.getHeaderPath());
            header.setNs(cc.getType().ns);
            header.setClass(cc);
            try {
                Util.writeHeader(header, dir);
            } catch (IOException e) {
                e.printStackTrace();
            }
            forwardHeader.forwardDeclarator.add(cc);
        }
        try {
            Util.writeHeader(forwardHeader, dir);
            System.out.println("wrote " + forwardHeader.getInclude());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
