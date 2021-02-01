package com.mesut.j2cpp.visitor;

import com.mesut.j2cpp.Config;
import com.mesut.j2cpp.ast.*;
import com.mesut.j2cpp.util.ArrayHelper;
import com.mesut.j2cpp.util.BindingMap;
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

        if (binding.isArray()) {
            return ArrayHelper.makeArrayType(fromBinding(binding.getElementType()), binding.getDimensions());
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
            if (binding.isNested() && Config.move_inners && !header.ns.getAll().isEmpty()) {//trim parent class ns
                type = new CType(header.ns.getAll() + "::" + binding.getName());
            }
            else {
                type = new CType(name);
            }

            /*for (ITypeBinding tp : binding.getTypeArguments()) {
                type.typeNames.add(fromBinding(tp));
            }*/
            if (!binding.isGenericType() && !binding.isNested()) {
                if (!binding.isFromSource() && header != null) {
                    if (header.source != null) {
                        header.source.addInclude(type);
                    }
                    //header.source.addInclude(name.replace('.', '/'));
                    //header.addInclude(name.replace(".", "/"));//inner classes too?
                }
            }
        }
        if (type.ns == null) {
            type.ns = new Namespace();
        }
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
        for (Type param : (List<Type>) n.typeArguments()) {
            CType arg = visit(param);
            if (Config.ptr_typeArg) {
                arg.setPointer(true);
            }
            arg.typeNames.clear();//c++ doesnt allow nested type args?
            type.typeNames.add(arg);
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
        cType.forward(header);
        //add to ref
        clazz.addType(cType);
        return cType;
    }
}
