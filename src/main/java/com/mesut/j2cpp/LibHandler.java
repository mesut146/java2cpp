package com.mesut.j2cpp;

import com.mesut.j2cpp.ast.CClass;
import com.mesut.j2cpp.ast.CField;
import com.mesut.j2cpp.ast.CHeader;
import com.mesut.j2cpp.ast.CMethod;
import com.mesut.j2cpp.ast.CName;
import com.mesut.j2cpp.ast.CParameter;
import com.mesut.j2cpp.ast.CType;
import com.mesut.j2cpp.map.ClassMap;
import com.mesut.j2cpp.util.ForwardDeclarator;
import com.mesut.j2cpp.visitor.PreVisitor;
import com.mesut.j2cpp.visitor.TypeVisitor;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

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
        if (binding == null) return;
        if (binding.isArray()) {
            addType(binding.getElementType());
            return;
        }
        binding = binding.getErasure();
        CClass cc = ClassMap.sourceMap.get(binding);
        if (cc != null) {
            PreVisitor.initType(binding, cc, null);
            if (!Config.useMappers) {
                ensureWrapperMembers(binding, cc);
            }
        }
        for (ITypeBinding arg : binding.getTypeArguments()) {
            addType(arg);
        }
    }

    //without a classpath JDT may see boxed primitives as empty stubs; add the
    //constructor and unboxing method the generated code needs
    private void ensureWrapperMembers(ITypeBinding binding, CClass cc) {
        String prim, valueMethod;
        switch (binding.getQualifiedName()) {
            case "java.lang.Boolean": prim = "bool"; valueMethod = "booleanValue"; break;
            case "java.lang.Byte": prim = "char"; valueMethod = "byteValue"; break;
            case "java.lang.Character": prim = "wchar_t"; valueMethod = "charValue"; break;
            case "java.lang.Short": prim = "char16_t"; valueMethod = "shortValue"; break;
            case "java.lang.Integer": prim = "int"; valueMethod = "intValue"; break;
            case "java.lang.Long": prim = "long"; valueMethod = "longValue"; break;
            case "java.lang.Float": prim = "float"; valueMethod = "floatValue"; break;
            case "java.lang.Double": prim = "double"; valueMethod = "doubleValue"; break;
            default: return;
        }
        boolean hasCons = false, hasValue = false;
        for (CMethod m : cc.methods) {
            if (m.isCons && m.params.size() == 1 && m.params.get(0).type != null
                    && m.params.get(0).type.type.equals(prim)) hasCons = true;
            if (m.name != null && m.name.is(valueMethod)) hasValue = true;
        }
        if (!hasCons) {
            CMethod cons = new CMethod();
            cons.isCons = true;
            cons.name = new CName(cc.name);
            cons.setPublic(true);
            cons.addParam(new CParameter(new CType(prim), new CName("v")));
            cc.addMethod(cons);
        }
        if (!hasValue) {
            CMethod value = new CMethod();
            value.name = new CName(valueMethod);
            value.type = new CType(prim);
            value.setPublic(true);
            cc.addMethod(value);
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
        if (!binding.isField() || binding.getDeclaringClass().isFromSource()) {
            return;
        }
        CClass cc = getClazz(binding.getDeclaringClass().getErasure());
        if (cc == null) return;//mapped type has no stub class
        for (CField field : cc.fields) {
            if (field.name.name.equals(binding.getName())) {
                return;
            }
        }
        CField field = new CField();
        field.setName(CName.from(binding.getName()));
        //register the field's type so stubs and forward declarations are generated for it
        addType(binding.getType());
        field.setType(TypeVisitor.fromBinding(binding.getType(), cc));
        field.setStatic(Modifier.isStatic(binding.getModifiers()));

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
