package com.mesut.j2cpp.visitor;

import com.mesut.j2cpp.Config;
import com.mesut.j2cpp.Logger;
import com.mesut.j2cpp.ast.CClass;
import com.mesut.j2cpp.ast.CType;
import com.mesut.j2cpp.ast.CUnionType;
import com.mesut.j2cpp.ast.Namespace;
import com.mesut.j2cpp.map.BindingMap;
import com.mesut.j2cpp.map.Mapper;
import com.mesut.j2cpp.util.ArrayHelper;
import com.mesut.j2cpp.util.TypeHelper;
import org.eclipse.jdt.core.dom.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

//visit types and ensures type is included
public class TypeVisitor {

    //set while emitting a templated method so its <T> is kept instead of erased
    public static boolean keepTypeVars = false;
    public static Set<String> activeTypeVars = new HashSet<>();

    //raw erasure of a type variable / wildcard: leftmost bound without type args
    public static CType erasedType(ITypeBinding binding) {
        ITypeBinding erasure = binding == null ? null : binding.getErasure();
        if (erasure == null || erasure.isTypeVariable() || erasure.isWildcardType()) {
            return TypeHelper.getObjectType();
        }
        String name = getBinaryName(erasure);
        CType raw = new CType(name);
        raw.realName = name;
        raw.binding = erasure;
        raw.fromSource = erasure.isFromSource();
        return raw;
    }

    public static CType fromBinding(ITypeBinding binding) {
        return fromBinding(binding, null);
    }

    public static CType fromBinding(ITypeBinding binding, CClass cc) {
        return fromBinding0(binding, cc);
    }

    public static CType fromBinding0(ITypeBinding binding, CClass cc) {
        if (binding.isArray()) {
            return ArrayHelper.makeArrayType(fromBinding0(binding.getElementType(), cc), binding.getDimensions());
        }
        if (binding.isWildcardType()) {
            return Mapper.instance.mapType(erasedType(binding), cc);
        }
        if (binding.isPrimitive()) {
            return new CType(TypeHelper.toCType(binding.getName()));
        }
        if (binding.isCapture()) {
            return Mapper.instance.mapType(erasedType(binding), cc);
        }

        CType type;
        if (binding.isTypeVariable()) {//<T>
            if (!Config.useMappers || binding.getDeclaringMethod() == null
                    || (keepTypeVars && activeTypeVars.contains(binding.getName()))) {
                //class type parameter, stub mode, or an in-scope method template
                CType t = new CType(binding.getName());
                t.isTemplate = true;
                t.isPointer = false;
                return t;
            }
            //method type parameter: Java erases it to its leftmost bound. Use a
            //raw type; expanding the bound's type arguments would recurse forever
            //for self-referential bounds such as <T extends Comparable<T>>.
            return Mapper.instance.mapType(erasedType(binding), cc);
        }
        else {
            String name = getBinaryName(binding);
            if (binding.isNested()) {
                String ns = binding.getPackage().getName();
                String rest = name.substring(ns.length() + 1);
                type = new CType(ns + "::" + rest.replace('.', '_'));
            }
            else {
                type = new CType(name);
            }
            type.realName = name;
            type.binding = binding;
            if (binding.isGenericType()) {
                for (ITypeBinding prm : binding.getTypeParameters()) {
                    type.typeNames.add(fromBinding(prm));
                }
            }
            else if (binding.isParameterizedType()) {
                for (ITypeBinding tp : binding.getTypeArguments()) {
                    CType tpp = fromBinding0(tp, cc);
                    tpp.isTypeArg = true;
                    type.typeNames.add(tpp);
                }
            }
            type.fromSource = binding.isFromSource();
        }
        type.isInner = binding.isNested();
        if (type.ns == null) {
            type.ns = new Namespace();
        }
        BindingMap.add(type, binding);
        type = Mapper.instance.mapType(type, cc);
        return type;
    }

    private static String getBinaryName(ITypeBinding binding) {
        String binaryName = binding.getBinaryName();
        String name;
        if (binaryName == null) {
            name = binding.getQualifiedName();
        }
        else {
            if (binaryName.contains("$")) {
                //inner
                name = binaryName.replace("$", ".");
            }
            else {
                name = binaryName;
            }
        }
        return name;
    }

    public static CType visit(PrimitiveType n) {
        return new CType(TypeHelper.toCType(n.toString()));
    }

    public static CType visit(SimpleType node) {
        ITypeBinding binding = node.resolveBinding();
        if (binding == null) {
            return new CType(node.getName().getFullyQualifiedName());
        }
        return fromBinding(binding);
    }

    public static CType visit(ArrayType n) {
        return ArrayHelper.makeArrayType(visit(n.getElementType()), n.getDimensions());
    }

    //<?>
    public static CType visit(WildcardType n) {
        if (n.getBound() != null) {
            return visit(n.getBound());//migrate?
        }
        return TypeHelper.getObjectType();
    }

    public static CType visit(ParameterizedType n) {
        CType type = visit(n.getType());
        if (n.typeArguments().isEmpty()) {

        }
        else {
            type.typeNames.clear();//override type names
            for (Type param : (List<Type>) n.typeArguments()) {
                CType arg = visit(param);
                if (Config.ptr_typeArg) {
                    arg.setPointer(true);
                }
                arg.typeNames.clear();//c++ doesn't allow nested type args?
                type.typeNames.add(arg);
            }
        }
        return type;
    }

    public static CType visit(UnionType n) {
        CUnionType unionType = new CUnionType();
        for (Type type : (List<Type>) n.types()) {
            unionType.types.add(visit(type));
        }
        return unionType;
    }

    public static CType visit(Type node) {
        CType type = null;
        if (node.isArrayType()) {
            type = visit((ArrayType) node);
        }
        else if (node.isSimpleType()) {
            type = visit((SimpleType) node);
        }
        else if (node.isParameterizedType()) {
            type = visit((ParameterizedType) node);
        }
        else if (node.isWildcardType()) {
            type = visit((WildcardType) node);
        }
        else if (node.isPrimitiveType()) {
            type = visit((PrimitiveType) node);
        }
        else if (node.isUnionType()) {
            type = visit((UnionType) node);
        }
        return type;
    }

    public static CType visitType(Type type, CClass cc) {
        ITypeBinding binding = type.resolveBinding();
        if (binding == null) {
            Logger.logBinding(cc, type.toString());
            return visit(type);
        }
        return fromBinding(binding, cc);
    }

    public interface Listener {
        void foundType(CType type);
    }
}
