package com.mesut.j2cpp.visitor;

import com.mesut.j2cpp.Config;
import com.mesut.j2cpp.ast.*;
import com.mesut.j2cpp.util.ArrayHelper;
import com.mesut.j2cpp.map.BindingMap;
import com.mesut.j2cpp.util.TypeHelper;
import org.eclipse.jdt.core.dom.*;

import java.util.List;

//visit types and ensures type is included
public class TypeVisitor {
    CHeader header;

    public TypeVisitor(CHeader header) {
        this.header = header;
    }

    public CType visit(PrimitiveType n) {
        return new CType(TypeHelper.toCType(n.toString()));
    }

    public CType fromBinding(ITypeBinding binding) {
        return fromBinding(binding, null);
    }

    public CType fromBinding(ITypeBinding binding, CClass cc) {
        if (binding.isArray()) {
            return ArrayHelper.makeArrayType(fromBinding(binding.getElementType(), cc), binding.getDimensions());
        }
        if (binding.isPrimitive()) {
            return new CType(TypeHelper.toCType(binding.getName()));
        }

        CType type;
        if (binding.isTypeVariable()) {//<T>
            type = new CType(binding.getName());
            type.isTemplate = true;
            type.isPointer = false;
        }
        else {
            String name = getBinaryName(binding);
            if (binding.isNested() && Config.move_inners) {//trim parent class ns
                type = new CType(binding.getPackage().getName().replace(".", "::") + "::" + binding.getName());
            }
            else {
                type = new CType(name);
            }

            if (binding.isGenericType()) {
                for (ITypeBinding prm : binding.getTypeParameters()) {
                    type.typeNames.add(fromBinding(prm));
                }
            }
            else if (binding.isParameterizedType()) {
                for (ITypeBinding tp : binding.getTypeArguments()) {
                    type.typeNames.add(fromBinding(tp, cc));
                }
            }

            if (!binding.isGenericType() && !binding.isNested()) {
                if (header != null) {
                    if (header.source != null) {
                        header.source.addInclude(type);
                    }
                }
            }
        }
        type.isInner = binding.isNested();
        if (type.ns == null) {
            type.ns = new Namespace();
        }
        if (cc != null) cc.addType(type);
        BindingMap.add(type, binding);
        return type;
    }

    private String getBinaryName(ITypeBinding binding) {
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

    public CType visit(SimpleType node) {
        //resolve
        ITypeBinding binding = node.resolveBinding();
        if (binding == null) {
            String str = node.getName().getFullyQualifiedName();
            return new CType(str);
        }
        return fromBinding(binding);
    }

    public CType visit(ArrayType n) {
        CType elemtype = visit(n.getElementType());
        return ArrayHelper.makeArrayType(elemtype, n.getDimensions());
    }

    public CType visit(WildcardType n) {
        //<?>
        if (n.getBound() != null) {
            return visit(n.getBound());
        }
        return new CType("java::lang::Object");
    }

    public CType visit(ParameterizedType n) {
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

    public CType visit(UnionType n) {
        CUnionType unionType = new CUnionType();
        for (Type type : (List<Type>) n.types()) {
            unionType.types.add(visit(type));
        }
        return unionType;
    }

    public CType visit(Type type) {
        CType cType = null;
        if (type.isArrayType()) {
            cType = visit((ArrayType) type);
        }
        else if (type.isSimpleType()) {
            cType = visit((SimpleType) type);
        }
        else if (type.isParameterizedType()) {
            cType = visit((ParameterizedType) type);
        }
        else if (type.isWildcardType()) {
            cType = visit((WildcardType) type);
        }
        else if (type.isPrimitiveType()) {
            cType = visit((PrimitiveType) type);
        }
        else if (type.isUnionType()) {
            cType = visit((UnionType) type);
        }
        return cType;
    }

    public CType visitType(Type type, CClass clazz) {
        CType cType = visit(type);
        //add to ref
        clazz.addType(cType);
        return cType;
    }
}
