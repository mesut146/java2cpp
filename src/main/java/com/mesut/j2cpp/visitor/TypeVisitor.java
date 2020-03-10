package com.mesut.j2cpp.visitor;

import com.mesut.j2cpp.Converter;
import com.mesut.j2cpp.Helper;
import com.mesut.j2cpp.ast.CClass;
import com.mesut.j2cpp.ast.CHeader;
import com.mesut.j2cpp.ast.CMethod;
import com.mesut.j2cpp.ast.CType;
import org.eclipse.jdt.core.dom.*;

//visit types and ensures type is included
public class TypeVisitor extends ASTVisitor {

    Converter converter;
    CHeader header;
    CType type;

    public TypeVisitor(Converter converter, CHeader header) {
        this.converter = converter;
        this.header = header;
    }

    public boolean visit(PrimitiveType n) {
        type = new CType(Helper.toCType(n.toString()));
        return false;
    }

    CType fromBinding(ITypeBinding binding) {
        if (binding.isTypeVariable()) {
            type=new CType(binding.getName());
            //System.out.printf("name=%s bin=%s qua=%s \n", binding.getName(),binding.getBinaryName(),binding.getQualifiedName());
            /*System.out.printf("bin=%s generic=%s raw=%s local=%s nested=%s typevar=%s prmtzd=%s\n",
                    bin, binding.isGenericType(), binding.isRawType(), binding.isLocal(), binding.isNested(), binding.isTypeVariable(),binding.isParameterizedType());
        */
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

            for (ITypeBinding tp : binding.getTypeArguments()) {
                type.typeNames.add(fromBinding(tp));
            }

            if (!binding.isGenericType() && !binding.isNested()) {
                header.addInclude(name.replace(".", "/"));//inner classes too?
            }
        }
        return type;
    }

    @Override
    public boolean visit(SimpleType node) {
        //resolve
        ITypeBinding binding = node.resolveBinding();
        //System.out.println("simple.type=" + binding.getBinaryName() + " nested=" + binding.isNested());
        type = fromBinding(binding);
        return false;
    }

    public boolean visit(ArrayType n) {
        n.getElementType().accept(this);
        //System.out.println("arr.type=" + n.getElementType().getClass());
        type = type.copy();
        type.dimensions = n.getDimensions();
        return false;
    }

    @Override
    public boolean visit(WildcardType n) {
        //<?>
        type = new CType("java::lang::Object");
        return false;
    }

    public boolean visit(ParameterizedType type) {
        type.getType().accept(this);
        /*for (Type param : (List<Type>) type.typeArguments()) {

        }*/
        return false;
    }

    //resolve type in a method,method type,param type,local type
    public CType visitType(Type type, CMethod method) {
        //System.out.println("m.type=" + type);
        if (type.isArrayType() || type.isParameterizedType()) {
            type.accept(this);
            return this.type.copy();
        }
        /*if (type.isSimpleType()) {

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
        return visit(ctype, null);*/
        return this.type;
    }

    //fields,methods,base class types,todo inner cls
    public CType visitType(Type type, CClass cc) {
        //System.out.println("type=" + type.getClass() + " " + type.toString());
        type.accept(this);
        /*if (type.isArrayType()) {//array type
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
        return visit(type.asClassOrInterfaceType(), null);*/
        return this.type;
    }
}
