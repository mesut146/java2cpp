package com.mesut.j2cpp;

import com.mesut.j2cpp.ast.*;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Modifier;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class LibImplHandler {
    public static LibImplHandler instance = new LibImplHandler();

    ClassMap classMap = new ClassMap();

    CType makeType(ITypeBinding binding) {
        return new CType(binding.getQualifiedName());
    }

    public void addMethod(IMethodBinding binding) {
        CType clazz = makeType(binding.getDeclaringClass());
        ClassMap.ClassDecl decl = classMap.get(clazz);
        for (CMethod method : decl.methods) {
            if (method.name.name.equals(binding.getName())) {
                return;
            }
        }
        System.out.println(binding.getDeclaringClass().getQualifiedName() + "." + binding.getName() + "()");
        CMethod method = new CMethod();
        method.name = CName.from(binding.getName());
        method.type = makeType(binding.getReturnType());
        method.isCons = binding.isConstructor();
        method.isPureVirtual = Modifier.isAbstract(binding.getModifiers());
        for (ITypeBinding p : binding.getParameterTypes()) {
            method.params.add(new CParameter(makeType(p), CName.from("p")));
        }
        decl.methods.add(method);
    }

    public void addField(IVariableBinding binding) {
        if (binding.getDeclaringClass() == null) {
            return;
        }
        if (!binding.isField() || !binding.isEnumConstant() || binding.getDeclaringClass().isFromSource()) {
            return;
        }
        CType clazz = makeType(binding.getDeclaringClass());
        ClassMap.ClassDecl decl = classMap.get(clazz);
        for (CField field : decl.fields) {
            if (field.name.name.equals(binding.getName())) {
                return;
            }
        }

        CField field = new CField();
        field.setName(CName.from(binding.getName()));
        field.setType(makeType(binding.getType()));

        decl.fields.add(field);
    }


    public void addType(CType type) {

    }

    public void writeAll(File dir) {
        for (ClassMap.ClassDecl decl : classMap.map.values()) {
            CHeader header = new CHeader(decl.type.basicForm().replace("::", "/") + ".h");
            header.ns = decl.type.ns;
            CClass cc = new CClass();
            cc.ns = header.ns;
            cc.name = decl.type.getName();
            for (CMethod method : decl.methods) {
                cc.addMethod(method);
            }
            for (CField field : decl.fields) {
                cc.addField(field);
            }
            header.addClass(cc);
            File path = new File(dir, header.rpath);
            path.getParentFile().mkdirs();
            try {
                Files.write(path.toPath(), header.toString().getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
