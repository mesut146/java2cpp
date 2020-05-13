package com.mesut.j2cpp.visitor;

import com.mesut.j2cpp.Converter;
import com.mesut.j2cpp.Helper;
import com.mesut.j2cpp.ast.CClass;
import com.mesut.j2cpp.ast.CHeader;
import com.mesut.j2cpp.ast.CMethod;
import com.mesut.j2cpp.ast.CType;
import org.eclipse.jdt.core.dom.*;

import java.util.List;

//visit types and ensures type is included
public class TypeVisitor {

    Converter converter;
    CHeader header;
    //CType type;

    public TypeVisitor(Converter converter, CHeader header) {
        this.converter = converter;
        this.header = header;
    }

    public CType visit(PrimitiveType n) {
        CType type = new CType(Helper.toCType(n.toString()));
        return type;
    }

    CType fromBinding(ITypeBinding binding) {
        CType type;

        if (binding.isTypeVariable()) {
            type = new CType(binding.getName());
        }
        else {
            String bin = binding.getBinaryName();
            String name;
            if (bin.contains("$")) {
                //inner
                name = bin.replace("$", ".");
            }
            else {
                name = bin;
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
        //System.out.println("type=" + node + " bind=" + binding);
        if (binding == null) {
            return new CType(node.getName().getFullyQualifiedName());
        }
        CType type = fromBinding(binding);
        return type;
    }

    public CType visit(ArrayType n) {
        CType type = visit(n.getElementType());
        type = type.copy();
        type.dimensions = n.getDimensions();
        return type;
    }

    public CType visit(WildcardType n) {
        //<?>
        CType type = new CType("java::lang::Object");
        return type;
    }

    public CType visit(ParameterizedType n) {
        //System.out.println("p.type=" + n.getType() + " " + n.typeArguments());
        CType type = visit(n.getType());
        for (Type param : (List<Type>) n.typeArguments()) {
            type.typeNames.add(visit(param));
        }
        return type;
    }

    //resolve type in a method,method type,param type,local type
    public CType visitType(Type type, CMethod method) {
        return visit(type);
    }

    public CType visit(Type type) {
        CType cType = null;
        if (type.isArrayType()) {
            cType = visit((ArrayType) type);
        }
        if (type.isSimpleType()) {
            cType = visit((SimpleType) type);
        }
        if (type.isParameterizedType()) {
            cType = visit((ParameterizedType) type);
        }
        if (type.isWildcardType()) {
            cType = visit((WildcardType) type);
        }
        if (type.isPrimitiveType()) {
            cType = visit((PrimitiveType) type);
        }
        //System.out.println("type=" + type + " res=" + cType + " cls=" + type.getClass());
        return cType;
    }

    //fields,methods,base class types,todo inner cls
    public CType visitType(Type type, CClass cc) {
        return visit(type);
    }
}
