package com.mesut.j2cpp.visitor;

import com.mesut.j2cpp.LibHandler;
import com.mesut.j2cpp.ast.*;
import com.mesut.j2cpp.map.ClassMap;
import com.mesut.j2cpp.map.Mapper;
import com.mesut.j2cpp.util.DepVisitor;
import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.List;

//parses all sources and generates classMap
public class PreVisitor {

    public static CClass initType(ITypeBinding binding, CClass cc, CClass outer) {
        if (cc.initialized) return cc;
        cc.isInterface = binding.isInterface();
        cc.isStatic = Modifier.isStatic(binding.getModifiers());
        cc.isPublic = Modifier.isPublic(binding.getModifiers());
        cc.isInner = outer != null;
        cc.fromSource = binding.isFromSource();
        for (ITypeBinding tp : binding.getTypeParameters()) {
            cc.template.add(new CType(tp.getName(), true));
        }
        if (binding.getSuperclass() != null) {
            cc.setSuper(TypeVisitor.fromBinding(binding.getSuperclass()));
            LibHandler.instance.addType(binding.getSuperclass());
        }
        for (ITypeBinding iface : binding.getInterfaces()) {
            cc.ifaces.add(TypeVisitor.fromBinding(iface));
            LibHandler.instance.addType(iface);
        }
        if (outer != null && !Modifier.isStatic(binding.getModifiers()) && !cc.isInterface) {
            InnerHelper.handleRef(cc, outer);
        }
        if (binding.isEnum()) {
            //add ordinal field
            CField ord = new CField();
            ord.type = new CType("int");
            ord.name = new CName("ordinal");
            cc.addField(ord);
        }
        cc.initialized = true;
        return cc;
    }

    public static CClass visitType(ITypeBinding binding, CClass outer) {
        CClass cc = initType(binding, ClassMap.sourceMap.get(binding), outer);
        if (cc.initMembers) return cc;
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
        cc.initMembers = true;
        return cc;
    }

    public static CMethod visitMethod(IMethodBinding binding, CClass cc) {
        if (cc == null) return null;
        if (!cc.fromSource) {
            binding = binding.getMethodDeclaration();
        }
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
        //method.name = new CName(binding.getName());
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
        /*if (superBinding.equals(binding.getDeclaringClass())){
            return;
        }*/
        //todo may be deeper
        for (IMethodBinding superMethod : superBinding.getDeclaredMethods()) {
            if (binding.isSubsignature(superMethod)) {
                ClassMap.sourceMap.getMethod(superMethod).setVirtual(true);
                //System.out.println("virtual parent " + superBinding.getQualifiedName() + " " + binding.getName());
                break;
            }
        }
    }

    //var binding to field
    public static CField visitField(IVariableBinding binding, CClass cc) {
        if (cc == null) return null;
        CType type = TypeVisitor.fromBinding(binding.getType(), cc);
        CField field = ClassMap.getAddedField(cc, binding, type);
        if (field != null) {
            return field;
        }
        field = new CField();
        cc.addField(field);
        if (Modifier.isStatic(binding.getModifiers()) && Modifier.isFinal(binding.getModifiers()) && binding.getType().isPrimitive()) {
            //constexpr
            field.set(ModifierNode.CONSTEXPR_NAME);
        }
        field.name = Mapper.instance.mapFieldName(binding.getName(), cc);
        field.type = type;
        field.setPublic(Modifier.isPublic(binding.getModifiers()));
        field.setStatic(Modifier.isStatic(binding.getModifiers()));
        field.setProtected(Modifier.isProtected(binding.getModifiers()));
        if (cc.isInterface) {
            field.setPublic(true);
        }
        if (binding.isEnumConstant()) {
            //todo
        }
        return field;
    }

    public void handle(CompilationUnit unit) {
        for (Object o : unit.types()) {
            AbstractTypeDeclaration decl = (AbstractTypeDeclaration) o;
            CClass cc = visitType(decl.resolveBinding(), null);
            new DepVisitor(cc, decl).handle();
        }
    }

    public Namespace visit(PackageDeclaration n) {
        if (n == null) return new Namespace();
        return new Namespace(n.getName().getFullyQualifiedName());
    }
}
