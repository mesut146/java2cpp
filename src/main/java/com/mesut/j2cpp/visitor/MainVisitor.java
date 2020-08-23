package com.mesut.j2cpp.visitor;

import com.mesut.j2cpp.Converter;
import com.mesut.j2cpp.ast.*;
import com.mesut.j2cpp.cppast.CExpression;
import com.mesut.j2cpp.cppast.expr.CClassInstanceCreation;
import com.mesut.j2cpp.util.Helper;
import org.eclipse.jdt.core.dom.*;

import java.util.List;
import java.util.Stack;

//visitor for single compilation unit
//fields,method decl,class decl
@SuppressWarnings("unchecked")
public class MainVisitor extends ASTVisitor {

    public CHeader header;
    public TypeVisitor typeVisitor;
    SourceVisitor sourceVisitor;
    public Stack<CClass> stack = new Stack<>();//class stack
    public Converter converter;

    public MainVisitor(Converter converter, CHeader header) {
        this.converter = converter;
        this.header = header;
        this.typeVisitor = new TypeVisitor(converter, header);
        this.sourceVisitor = new SourceVisitor(converter, header.source);
    }

    public CClass last() {
        return stack.peek();
    }

    CClass parent() {
        if (stack.isEmpty()) {
            return null;
        }
        return last();
    }

    @Override
    public boolean visit(PackageDeclaration n) {
        Namespace ns = new Namespace();
        ns.fromPkg(n.getName().getFullyQualifiedName());
        header.ns = ns;
        header.using.add(header.ns);
        header.source.usings.add(header.ns);
        return true;
    }

    @Override
    public boolean visit(ImportDeclaration node) {
        String imp = node.getName().getFullyQualifiedName();

        imp = imp.replace(".", "/");
        if (node.isStatic()) {
            //base.cls.var;
            int idx = imp.lastIndexOf("/");
            if (idx != -1) {
                imp = imp.substring(0, idx - 1);
            }
            header.includes.add(imp);
        }
        if (node.isOnDemand()) {
            //TODO
            //resolve seperately
        }
        else {
            header.includes.add(imp);
        }
        return true;
    }


    //class or interface
    @Override
    public boolean visit(TypeDeclaration node) {
        //System.out.println("type.decl=" + node.getName());
        CClass cc = new CClass();
        if (stack.size() == 0) {
            header.addClass(cc);
        }
        else {
            last().addInner(cc);
        }
        stack.push(cc);

        cc.name = node.getName().getFullyQualifiedName();
        cc.isInterface = node.isInterface();


        node.typeParameters().forEach(type -> cc.template.add(new CType(type.toString(), true).setHeader(header)));
        if (node.getSuperclassType() != null) {
            CType baseType = typeVisitor.visitType(node.getSuperclassType());
            baseType.isTemplate = false;
            baseType.isPointer = false;
            cc.base.add(baseType);
        }

        node.superInterfaceTypes().forEach(iface -> {
            CType ifType = typeVisitor.visitType((Type) iface);
            ifType.isTemplate = false;
            ifType.isPointer = false;
            cc.base.add(ifType);
        });
        for (FieldDeclaration field : node.getFields()) {
            field.accept(this);
        }
        for (MethodDeclaration method : node.getMethods()) {
            method.accept(this);
        }
        //inner classes
        for (TypeDeclaration member : node.getTypes()) {
            member.accept(this);
        }
        node.bodyDeclarations().forEach(body -> {
            if (body instanceof EnumDeclaration) {
                ((EnumDeclaration) body).accept(this);
            }
        });
        /*for (BodyDeclaration body : (List<BodyDeclaration>) node.bodyDeclarations()) {
            body.accept(this);
        }*/
        stack.pop();
        return false;
    }

    public boolean visit(EnumDeclaration n) {
        //System.out.println("enum.decl=" + n.getName());
        CClass cc = new CClass();
        if (stack.size() == 0) {
            header.addClass(cc);
        }
        else {
            last().addInner(cc);
        }
        stack.push(cc);

        cc.isEnum = true;
        cc.name = n.getName().getFullyQualifiedName();
        cc.base.add(Helper.getEnumType().setHeader(header));
        header.addInclude("java/lang/Enum");

        n.superInterfaceTypes().forEach(iface -> cc.base.add(typeVisitor.visit((Type) iface)));

        for (EnumConstantDeclaration constant : (List<EnumConstantDeclaration>) n.enumConstants()) {
            CField field = new CField();
            cc.addField(field);
            field.setPublic(true);
            field.setStatic(true);
            field.type = new CType(cc.name, header);
            field.setName(constant.getName().getIdentifier());

            if (!constant.arguments().isEmpty()) {
                CClassInstanceCreation args = new CClassInstanceCreation();
                args.type = field.type;
                for (Expression val : (List<Expression>) constant.arguments()) {
                    args.args.add((CExpression) sourceVisitor.visitExpr(val, null));
                }
                field.expression = args;
            }
            else {
                //todo make ordinals
                //throw new RuntimeException("ordinal");
            }

            if (constant.getAnonymousClassDeclaration() != null) {
                throw new RuntimeException("anonymous enum constant is not supported");
            }
        }
        if (!n.bodyDeclarations().isEmpty()) {
            n.bodyDeclarations().forEach(p -> ((BodyDeclaration) p).accept(this));
        }
        //todo put values and valueOf method
        stack.pop();
        return false;
    }

    @Override
    public boolean visit(FieldDeclaration n) {
        CType type = typeVisitor.visitType(n.getType());
        if (converter.debug_fields)
            System.out.println("field.decl=" + n.getType() + " " + n.fragments());
        for (VariableDeclarationFragment frag : (List<VariableDeclarationFragment>) n.fragments()) {
            CField field = new CField();
            last().addField(field);
            field.type = type;
            field.setName(frag.getName().getIdentifier());
            field.setPublic(Modifier.isPublic(n.getModifiers()));
            field.setStatic(Modifier.isStatic(n.getModifiers()));
            field.setProtected(Modifier.isProtected(n.getModifiers()));
            if (last().isInterface) {
                field.setPublic(true);
            }
            if (frag.getInitializer() != null) {
                sourceVisitor.clazz = last();
                field.expression = (CExpression) sourceVisitor.visitExpr(frag.getInitializer(), null);
                sourceVisitor.clazz = null;
            }
        }
        return false;
    }

    @Override
    public boolean visit(MethodDeclaration n) {
        if (converter.debug_methods)
            System.out.println("method.decl=" + n.getReturnType2() + " " + n.getName() + "()" + " cons=" + n.isConstructor() + " prent=" + parent().name);
        CMethodDecl method = new CMethodDecl();
        last().addMethod(method);

        n.typeParameters().forEach(temp -> method.template.add(new CType(temp.toString(), true)));

        if (n.isConstructor()) {
            method.isCons = true;
        }
        else {
            Type type = n.getReturnType2();
            if (type == null) {
                method.isCons = true;
            }
            else {
                method.type = typeVisitor.visitType(n.getReturnType2());
            }
        }

        //type could be template

        method.name = new CName(n.getName().getIdentifier());

        method.setStatic(Modifier.isStatic(n.getModifiers()));
        method.setPublic(Modifier.isPublic(n.getModifiers()));
        method.setNative(Modifier.isNative(n.getModifiers()));
        if (last().isInterface || Modifier.isAbstract(n.getModifiers())) {
            method.setPublic(true);
            method.isPureVirtual = true;
        }

        for (SingleVariableDeclaration param : (List<SingleVariableDeclaration>) n.parameters()) {
            CParameter cp = new CParameter();
            cp.method = method;
            cp.type = typeVisitor.visitType(param.getType());
            cp.type.isTemplate = false;
            cp.setName(param.getName().getIdentifier());
            method.addParam(cp);
        }

        method.node = n;
        return false;
    }


    //static block
    /*@Override
    public void visit(InitializerDeclaration n, Writer w) {
        if (n.isStatic()) {
            header.includePath("static_block.hpp");
            w = new Writer();
            last().staticBlock = w;
            w.append("static_block");
            n.getBody().accept(statementVisitor, w);
        }
    }*/
}
