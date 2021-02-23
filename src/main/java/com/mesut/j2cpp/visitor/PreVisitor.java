package com.mesut.j2cpp.visitor;

import com.mesut.j2cpp.ast.*;
import com.mesut.j2cpp.map.ClassMap;
import com.mesut.j2cpp.util.DepVisitor;
import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.List;

//parses all sources and generates classMap
public class PreVisitor {

    public static CClass visitType(ITypeBinding binding, CClass outer) {
        CClass cc = ClassMap.sourceMap.get(TypeVisitor.fromBinding(binding));
        if (binding.getSuperclass() != null) {
            cc.setSuper(TypeVisitor.fromBinding(binding.getSuperclass()));
        }
        cc.isInterface = binding.isInterface();
        cc.isStatic = Modifier.isStatic(binding.getModifiers());
        cc.isPublic = Modifier.isPublic(binding.getModifiers());
        cc.isInner = outer != null;
        for (ITypeBinding tp : binding.getTypeParameters()) {
            cc.template.add(new CType(tp.getName(), true));
        }
        for (ITypeBinding tp : binding.getInterfaces()) {
            cc.addBase(TypeVisitor.fromBinding(tp));
        }
        if (outer != null && !Modifier.isStatic(binding.getModifiers()) && !cc.isInterface) {
            InnerHelper.handleRef(cc, outer);
        }
        //members
        for (IMethodBinding methodBinding : binding.getDeclaredMethods()) {
            visitMethod(methodBinding, cc);
        }
        for (ITypeBinding inner : binding.getDeclaredTypes()) {
            visitType(inner, cc);
        }
        for (IVariableBinding field : binding.getDeclaredFields()) {
            visitField(field, cc);
        }
        return cc;
    }

    public static CMethod visitMethod(IMethodBinding binding, CClass cc) {
        List<CType> params = new ArrayList<>();
        for (ITypeBinding prm : binding.getParameterTypes()) {
            params.add(TypeVisitor.fromBinding(prm, cc));
        }
        CType type = TypeVisitor.fromBinding(binding.getReturnType());
        CMethod method = ClassMap.getAddedMethod(cc, binding, params, type);
        if (method != null) {
            return method;
        }
        method = new CMethod();
        method.name = new CName(binding.getName());
        method.type = type;
        method.isCons = binding.isConstructor();

        method.setStatic(Modifier.isStatic(binding.getModifiers()));
        method.setPublic(Modifier.isPublic(binding.getModifiers()));
        method.setNative(Modifier.isNative(binding.getModifiers()));
        if (cc.isInterface || Modifier.isAbstract(binding.getModifiers())) {
            method.isPureVirtual = true;
            method.setPublic(true);
            method.setVirtual(true);
        }

        for (int i = 0; i < params.size(); i++) {
            method.addParam(new CParameter(params.get(i), new CName("p" + i)));
        }
        for (ITypeBinding tp : binding.getTypeParameters()) {
            method.template.add(new CType(tp.getName(), true));
        }

        handleVirtual(binding);

        cc.addMethod(method);
        return method;
    }

    private static void handleVirtual(IMethodBinding binding) {
        //find super method and set virtual
        ITypeBinding superBinding = binding.getDeclaringClass().getSuperclass();
        if (superBinding == null) return;
        //todo may be deeper
        for (IMethodBinding superMethod : superBinding.getDeclaredMethods()) {
            if (binding.isSubsignature(superMethod)) {
                ClassMap.sourceMap.getMethod(superMethod).setVirtual(true);
                //System.out.println("virtual parent " + superBinding.getQualifiedName() + " " + binding.getName());
                break;
            }
        }
    }

    public static CField visitField(IVariableBinding binding, CClass cc) {
        CType type = TypeVisitor.fromBinding(binding.getType(), cc);
        CField field = ClassMap.getAddedField(cc, binding, type);
        if (field != null) {
            return field;
        }
        field = new CField();
        field.name = CName.from(binding.getName());
        field.type = type;
        field.setPublic(Modifier.isPublic(binding.getModifiers()));
        field.setStatic(Modifier.isStatic(binding.getModifiers()));
        field.setProtected(Modifier.isProtected(binding.getModifiers()));
        if (cc.isInterface) {
            field.setPublic(true);
        }
        cc.addField(field);
        return field;
    }

    public void handle(CompilationUnit unit) {
        Namespace ns = visit(unit.getPackage());
        for (AbstractTypeDeclaration decl : (List<AbstractTypeDeclaration>) unit.types()) {
            if (decl instanceof EnumDeclaration) {
                visitType(decl.resolveBinding(), null);
            }
            else if (decl instanceof AnnotationTypeDeclaration) {
                //todo
                System.out.println("anno " + unit.getPackage().getName() + " " + decl.getName());
            }
            else {
                visitType(decl.resolveBinding(), null);
            }
        }
    }

    public Namespace visit(PackageDeclaration n) {
        if (n == null) return new Namespace();
        return new Namespace(n.getName().getFullyQualifiedName());
    }
}
