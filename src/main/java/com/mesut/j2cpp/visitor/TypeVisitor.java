package com.mesut.j2cpp.visitor;

import com.mesut.j2cpp.Config;
import com.mesut.j2cpp.ast.CArrayType;
import com.mesut.j2cpp.ast.CHeader;
import com.mesut.j2cpp.ast.CType;
import com.mesut.j2cpp.ast.CUnionType;
import com.mesut.j2cpp.util.Helper;
import org.eclipse.jdt.core.dom.*;

import java.util.List;

//visit types and ensures type is included
public class TypeVisitor {

    CHeader header;

    public TypeVisitor(CHeader header) {
        this.header = header;
    }

    public CType visit(PrimitiveType n) {
        return new CType(Helper.toCType(n.toString()));
    }

    CType fromBinding(ITypeBinding binding) {
        CType type;

        if (binding.isTypeVariable()) {
            type = new CType(binding.getName());
            type.isTemplate = true;
            type.isPointer = false;
        }
        else {
            String bin = binding.getBinaryName();
            String name;
            if (bin == null) {
                name = binding.getQualifiedName();
            }
            else {
                if (bin.contains("$")) {
                    //inner
                    name = bin.replace("$", ".");
                }
                else {
                    name = bin;
                }
            }


            type = new CType(name.replace(".", "::"));

            /*for (ITypeBinding tp : binding.getTypeArguments()) {
                type.typeNames.add(fromBinding(tp));
            }*/

            if (!binding.isGenericType() && !binding.isNested()) {
                header.addInclude(name.replace(".", "/"));//inner classes too?
            }
        }
        return type;
    }

    public CType visit(SimpleType node) {
        //resolve
        ITypeBinding binding = node.resolveBinding();
        if (binding == null) {
            String str = node.getName().getFullyQualifiedName();
            return new CType(str.replace(".", "::"));
        }
        return fromBinding(binding);
    }

    public CType visit(ArrayType n) {
        CType type = visit(n.getElementType());
        type.setPointer(true);//?
        return new CArrayType(type, n.getDimensions());
    }

    public CType visit(WildcardType n) {
        //<?>
        return new CType("java::lang::Object");
    }

    public CType visit(ParameterizedType n) {
        CType type = visit(n.getType());
        for (Type param : (List<Type>) n.typeArguments()) {
            CType arg = visitType(param);
            if (Config.ptr_typeArg) {
                arg.setPointer(true);
            }
            type.typeNames.add(arg);
        }
        return type;
    }

    public CType visit(UnionType n) {
        CUnionType unionType = new CUnionType();
        for (Type type : (List<Type>) n.types()) {
            unionType.types.add(visitType(type));
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
        //System.out.println("type=" + type + " res=" + cType + " cls=" + type.getClass());
        return cType;
    }

    public CType visitType(Type type) {
        CType cType = visit(type);
        cType.setHeader(header);
        cType.forward();
        return cType;
    }
}
