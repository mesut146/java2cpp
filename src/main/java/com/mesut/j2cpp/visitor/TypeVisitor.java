package com.mesut.j2cpp.visitor;

import com.mesut.j2cpp.Converter;
import com.mesut.j2cpp.Helper;
import com.mesut.j2cpp.Writer;
import com.mesut.j2cpp.ast.CClass;
import com.mesut.j2cpp.ast.CHeader;
import com.mesut.j2cpp.ast.CMethod;
import com.mesut.j2cpp.ast.CType;
import org.eclipse.jdt.core.dom.*;

//visit types and ensures type is included
public class TypeVisitor extends GenericVisitor<CType, Writer> {

    Converter converter;
    CHeader header;

    public TypeVisitor(Converter converter, CHeader header) {
        this.converter = converter;
        this.header = header;
    }

    public CType visit(PrimitiveType n, Writer w) {
        return new CType(Helper.toCType(n.toString()));
    }

    public CType visit(ArrayType n, Writer w) {
        CType type = visit(n.getElementType(), w).copy();
        type.arrayLevel = n.getDimensions();
        return type;
    }

    /*public CType visit(Void n, Writer w) {
        return new CType("void");
    }*/

    /*public CType visit(ClassOrInterfaceType n, Writer w) {
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

        header.addInclude(q.replace(".", "/"));//inner classes too?
        return type;
    }*/

    //multi catch type
    /*public CType visit(UnionType n, Writer w) {
        CType type = visit(n.types().get(0),w);
        System.out.println("union type detected and chosen the first");
        return type;
    }*/

    public CType visit(WildcardType n, Writer arg) {
        //<?>
        return new CType("java::lang::Object");
    }


    public CType visit(TypeParameter typeParameter, Writer w) {
        return new CType(typeParameter.getName().getIdentifier());
    }

    //resolve type in a method,method type,param type,local type
    public CType visitType(Type type, CMethod method) {
        if (type.isArrayType()) {
            return visit((ArrayType) type, null).copy();
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
            return visit((ArrayType) type, null).copy();
        }
        if (!type.isClassOrInterfaceType()) {//void or primitive
            return type.accept(this, null);
        }
        for (CType ct : cc.getTemplate().getList()) {//class temptated type
            if (ct.getName().equals(type.toString())) {
                return ct.copy();
            }
        }
        return visit(type.asClassOrInterfaceType(), null);
    }
}
