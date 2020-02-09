package com.mesut.j2cpp.visitor;

import com.github.javaparser.ast.type.*;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;
import com.mesut.j2cpp.Converter;
import com.mesut.j2cpp.Helper;
import com.mesut.j2cpp.Nodew;
import com.mesut.j2cpp.ast.CHeader;
import com.mesut.j2cpp.ast.CType;

import java.util.Iterator;

public class TypeVisitor extends GenericVisitorAdapter<Object, Nodew> {

    Converter converter;
    CHeader header;

    public TypeVisitor(Converter converter, CHeader header) {
        this.converter = converter;
        this.header = header;
    }

    public Object visit(PrimitiveType n, Nodew w) {
        CType typeName = new CType(Helper.toCType(n.asString()));
        //w.append(typeName.toString());
        return typeName;
    }

    public Object visit(ArrayType n, Nodew w) {
        CType typeName = (CType) n.getElementType().accept(this, w);
        typeName.arrayLevel = n.getArrayLevel();
        return typeName;
    }

    public Object visit(VoidType n, Nodew w) {
        CType typeName = new CType("void");
        //w.append(typeName.toString());
        return typeName;
    }

    public Object visit(ClassOrInterfaceType n, Nodew w) {
        CType type = converter.getResolver().resolveType(n.getNameAsString(), header);
        if (type != null) {
            if (!header.isIncluded(type.getIncludePath())) {
                header.includes.add(type.getIncludePath());
            }
        } else {
            System.err.println("type : " + n.getNameAsString() + " not found");
            return null;
        }

        CType typeName = new CType(n.getNameAsString());
        if (n.getTypeArguments().isPresent()) {
            for (Iterator<Type> iterator = n.getTypeArguments().get().iterator(); iterator.hasNext(); ) {
                typeName.typeNames.add((CType) iterator.next().accept(this, w));
            }
        }
        if (n.getScope().isPresent()) {//todo
            CType scope = (CType) n.getScope().get().accept(this, new Nodew());
            typeName.type = scope.type + "::" + typeName.type;
        }
        //w.append(typeName.toString());
        return typeName;
    }

    public Object visit(UnionType n, Nodew w) {
        CType type = (CType) n.getElements().get(0).accept(this, new Nodew());
        System.out.println("union type detected and chosen the first");
        return type;
    }

    @Override
    public Object visit(WildcardType n, Nodew arg) {
        CType type = new CType("java::lang::Object");
        //type.ns=new Namespace("java::lang");
        return type;
    }
}
