package com.mesut.j2cpp.visitor;

import com.github.javaparser.ast.type.*;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.mesut.j2cpp.Converter;
import com.mesut.j2cpp.Helper;
import com.mesut.j2cpp.Writer;
import com.mesut.j2cpp.ast.CClass;
import com.mesut.j2cpp.ast.CHeader;
import com.mesut.j2cpp.ast.CMethod;
import com.mesut.j2cpp.ast.CType;

//visit types and ensures type is included
public class TypeVisitor extends GenericVisitorAdapter<CType, Writer> {

    Converter converter;
    CHeader header;

    public TypeVisitor(Converter converter, CHeader header) {
        this.converter = converter;
        this.header = header;
    }

    public CType visit(PrimitiveType n, Writer w) {
        return new CType(Helper.toCType(n.asString()));
    }

    public CType visit(ArrayType n, Writer w) {
        CType type = n.getElementType().accept(this, w).copy();
        type.arrayLevel = n.getArrayLevel();
        return type;
    }

    public CType visit(VoidType n, Writer w) {
        return new CType("void");
    }

    public CType visit(ClassOrInterfaceType n, Writer w) {
        //if no classpath,include it directly
        String q;
        CType type;
        if (converter.classpath.isEmpty()) {
            q = n.getNameAsString();
            type = new CType(q.replace(".", "::"));
        } else {
            ResolvedReferenceType resolved = n.resolve();
            q = resolved.getQualifiedName();

            type = new CType(q.replace(".", "::"));
            n.getTypeArguments().ifPresent(list -> list.forEach(tp -> type.typeNames.add(new CType(tp.toString(), true))));
        }

        header.addInclude(q.replace(".", "/"));
        return type;
    }

    //multi catch type
    public CType visit(UnionType n, Writer w) {
        CType type = n.getElements().get(0).accept(this, null);
        System.out.println("union type detected and chosen the first");
        return type;
    }

    @Override
    public CType visit(WildcardType n, Writer arg) {
        //<?>
        return new CType("java::lang::Object");
    }

    @Override
    public CType visit(TypeParameter typeParameter, Writer w) {
        return new CType(typeParameter.asString());
    }

    //resolve type in a method,method type,param type,local type
    public CType visitType(Type type, CMethod method) {
        if (type.isArrayType()) {
            CType cType = visitType(type.getElementType(), method).copy();
            cType.arrayLevel = type.getArrayLevel();
            //cType.pointer=true;
            return cType;
        }
        if (!type.isClassOrInterfaceType()) {
            return type.accept(this, null);
        }
        //type could be type param,Class type reference or normal type
        ClassOrInterfaceType ctype = type.asClassOrInterfaceType();
        String name = ctype.getNameAsString();
        for (CType ct : method.getTemplate().getList()) {
            if (ct.getName().equals(name)) {
                return ct.copy();
            }
        }
        for (CType ct : method.getParent().getTemplate().getList()) {
            if (ct.getName().equals(name)) {
                return ct.copy();
            }
        }
        //it has to be declared type
        return visit(ctype, null);
    }

    //fields,methods,base class types,todo inner cls
    public CType visitType(Type type, CClass cc) {
        if (type.isArrayType()) {//array type
            CType cType = visitType(type.getElementType(), cc).copy();
            cType.arrayLevel = type.getArrayLevel();
            return cType;
        }
        if (!type.isClassOrInterfaceType()) {//void or primitive
            return type.accept(this, null);
        }
        for (CType ct : cc.getTemplate().getList()) {//class temptated type
            if (ct.getName().equals(type.asString())) {
                return ct.copy();
            }
        }
        return visit(type.asClassOrInterfaceType(), null);
    }
}
