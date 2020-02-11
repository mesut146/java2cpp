package com.mesut.j2cpp.visitor;

import com.github.javaparser.ast.type.*;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.mesut.j2cpp.Converter;
import com.mesut.j2cpp.Helper;
import com.mesut.j2cpp.Nodew;
import com.mesut.j2cpp.ast.CClass;
import com.mesut.j2cpp.ast.CHeader;
import com.mesut.j2cpp.ast.CMethod;
import com.mesut.j2cpp.ast.CType;

//visit types and ensures type is included
public class TypeVisitor extends GenericVisitorAdapter<Object, Nodew> {

    Converter converter;
    CHeader header;

    public TypeVisitor(Converter converter, CHeader header) {
        this.converter = converter;
        this.header = header;
    }

    public Object visit(PrimitiveType n, Nodew w) {
        return new CType(Helper.toCType(n.asString()));
    }

    public Object visit(ArrayType n, Nodew w) {
        CType type = (CType) n.getElementType().accept(this, w);
        type.arrayLevel = n.getArrayLevel();
        return type;
    }

    public Object visit(VoidType n, Nodew w) {
        return new CType("void");
    }

    public Object visit(ClassOrInterfaceType n, Nodew w) {
        System.out.println("solving=" + n.getNameAsString());

        ResolvedReferenceType resolved = n.resolve();
        //System.out.printf("q=%s id=%s\n", resolved.getQualifiedName(), resolved.getId());
        String q = resolved.getQualifiedName();
        String[] arr = q.split(".");
        CType type = new CType(q.replace(".", "::"));
        header.addInclude(q.replace(".", "/"));
        return type;

        /*CType type = converter.getResolver().resolveType(n.getNameAsString(), header);
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
            for (Type value : n.getTypeArguments().get()) {
                typeName.typeNames.add((CType) value.accept(this, w));
            }
        }
        if (n.getScope().isPresent()) {//todo
            CType scope = (CType) n.getScope().get().accept(this, new Nodew());
            typeName.type = scope.type + "::" + typeName.type;
        }

        return typeName;*/
    }

    //multi catch type
    public Object visit(UnionType n, Nodew w) {
        CType type = (CType) n.getElements().get(0).accept(this, new Nodew());
        System.out.println("union type detected and chosen the first");
        return type;
    }

    @Override
    public Object visit(WildcardType n, Nodew arg) {
        //<?>
        return new CType("java::lang::Object");
    }

    @Override
    public Object visit(TypeParameter typeParameter, Nodew w) {
        return new CType(typeParameter.asString());
    }

    //resolve type in a method,method type,param type,local type
    public CType visitType(Type type, CMethod method) {
        if (type.isArrayType()) {
            CType cType = visitType(type.getElementType(), method);
            cType.arrayLevel = type.getArrayLevel();
            return cType;
        }
        if (!type.isClassOrInterfaceType()) {
            return (CType) type.accept(this, new Nodew());
        }
        //type could be type param,Class type reference or normal type
        ClassOrInterfaceType ctype = type.asClassOrInterfaceType();
        String name = ctype.getNameAsString();

        for (CType ct : method.getTemplate().getList()) {
            if (ct.getName().equals(name)) {
                return new CType(name);
            }
        }
        for (CType ct : method.getParent().getTemplate().getList()) {
            //System.out.println("ct=" + ct.getName());
            if (ct.getName().equals(name)) {
                return new CType(name);
            }
        }
        //it has to be declared type
        return (CType) ctype.accept(this, new Nodew());
    }

    public CType visitType(Type type, CClass cc) {
        if (type.isArrayType()) {
            CType cType = visitType(type.getElementType(), cc);
            cType.arrayLevel = type.getArrayLevel();
            return cType;
        }
        if (!type.isClassOrInterfaceType()) {
            return (CType) type.accept(this, new Nodew());
        }
        for (CType ct : cc.getTemplate().getList()) {
            if (ct.getName().equals(type.asString())) {//class templated field type
                return new CType(type.asString());
            }
        }
        return (CType) type.accept(this, new Nodew());
    }

    <my> void asd() {
        my asd;
    }
}
