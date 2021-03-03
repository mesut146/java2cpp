package com.mesut.j2cpp.visitor;

import com.mesut.j2cpp.Config;
import com.mesut.j2cpp.Logger;
import com.mesut.j2cpp.ast.*;
import com.mesut.j2cpp.map.ClassMap;
import com.mesut.j2cpp.map.Mapper;
import com.mesut.j2cpp.util.ArrayHelper;
import com.mesut.j2cpp.map.BindingMap;
import com.mesut.j2cpp.util.TypeHelper;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.internal.core.util.BindingKeyResolver;

import java.util.Arrays;
import java.util.List;

//visit types and ensures type is included
public class TypeVisitor {

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
            return TypeHelper.getObjectType();
        }
        if (binding.isPrimitive()) {
            return new CType(TypeHelper.toCType(binding.getName()));
        }
        if (binding.isCapture()) {
            ITypeBinding[] arr = binding.getTypeBounds();
            if (arr.length == 1) {
                return fromBinding(arr[0]);
            }
            System.out.println("capture with " + binding);
            return TypeHelper.getObjectType();
        }

        CType type;
        if (binding.isTypeVariable()) {//<T>
            type = new CType(binding.getName());
            type.isTemplate = true;
            type.isPointer = false;
            return type;
        }
        else {
            String name = getBinaryName(binding);
            if (binding.isNested()) {//trim parent class ns
                type = new CType(binding.getPackage().getName().replace(".", "::") + "::" + binding.getName());
            }
            else {
                type = new CType(name);
            }
            type.realName = name;
            if (binding.isGenericType()) {
                for (ITypeBinding prm : binding.getTypeParameters()) {
                    type.typeNames.add(fromBinding(prm));
                }
            }
            else if (binding.isParameterizedType()) {
                for (ITypeBinding tp : binding.getTypeArguments()) {
                    type.typeNames.add(fromBinding0(tp, cc));
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
