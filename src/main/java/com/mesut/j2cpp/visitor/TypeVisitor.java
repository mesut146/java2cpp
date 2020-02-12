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
        //System.out.println("solving=" + n.getNameAsString());
        if (converter.jars.isEmpty() && converter.cpDirs.isEmpty()) {
            String q = n.getNameAsString();
            CType type = new CType(q.replace(".", "::"));
            header.addInclude(q.replace(".", "/"));
            return type;
        }
        ResolvedReferenceType resolved = n.resolve();
        String q = resolved.getQualifiedName();
        CType type = new CType(q.replace(".", "::"));
        header.addInclude(q.replace(".", "/"));
        return type;
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
            //cType.pointer=true;
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
                return ct;
            }
        }
        for (CType ct : method.getParent().getTemplate().getList()) {
            if (ct.getName().equals(name)) {
                return ct;
            }
        }
        //it has to be declared type
        return (CType) ctype.accept(this, new Nodew());
    }

    //for class members; fields,methods,todo inner cls
    public CType visitType(Type type, CClass cc) {
        if (type.isArrayType()) {//array type
            CType cType = visitType(type.getElementType(), cc);
            cType.arrayLevel = type.getArrayLevel();
            return cType;
        }
        if (!type.isClassOrInterfaceType()) {//normal type
            return (CType) type.accept(this, new Nodew());
        }
        for (CType ct : cc.getTemplate().getList()) {//class temptlated type
            if (ct.getName().equals(type.asString())) {//class templated field type
                return ct;
            }
        }
        return (CType) type.accept(this, new Nodew());
    }
}
