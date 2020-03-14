package com.mesut.j2cpp.visitor;

import com.mesut.j2cpp.Converter;
import com.mesut.j2cpp.Writer;
import com.mesut.j2cpp.ast.*;
import org.eclipse.jdt.core.dom.*;

import java.util.List;
import java.util.Stack;

//visitor for single compilation unit
//fields,method decl,class decl
public class MainVisitor extends ASTVisitor {

    public CHeader header;
    //public MethodVisitor methodVisitor;
    public TypeVisitor typeVisitor;
    public StatementVisitor statementVisitor;
    public ExprVisitor exprVisitor;
    public Stack<CClass> stack = new Stack<>();
    public Converter converter;

    public MainVisitor(Converter converter, CHeader header) {
        this.converter = converter;
        this.header = header;
        this.typeVisitor = new TypeVisitor(converter, header);
        this.exprVisitor = new ExprVisitor(converter, header, typeVisitor);
        this.statementVisitor = new StatementVisitor(converter, header, exprVisitor, typeVisitor);
    }

    public CClass last() {
        return stack.peek();
    }

    @Override
    public boolean visit(PackageDeclaration n) {
        Namespace ns = new Namespace();
        ns.pkg(n.getName().getFullyQualifiedName());
        header.ns = ns;
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
            header.importStar.add(imp);
        }
        else {
            header.includes.add(imp);
        }
        return true;
    }

    @Override
    public boolean visit(TypeDeclaration node) {

        CClass cc = new CClass();
        if (stack.size() == 0) {
            header.addClass(cc);
            //System.out.println("type.decl=" + node.getName());
        }
        else {
            //System.out.println("type.decl=" + node.getName() + " parent=" + last().name);
            last().addInner(cc);
        }
        stack.push(cc);

        exprVisitor.clazz = cc;
        cc.name = node.getName().getFullyQualifiedName();
        cc.isInterface = node.isInterface();


        node.typeParameters().forEach(type -> cc.template.add(new CType(type.toString(), true)));
        if (node.getSuperclassType() != null) {
            CType baseType = typeVisitor.visitType(node.getSuperclassType(), cc);
            baseType.isTemplate = false;
            baseType.isPointer = false;
            cc.base.add(baseType);
        }


        node.superInterfaceTypes().forEach(iface -> {
            /*CType ifType = typeVisitor.visitType(iface, cc);
            ifType.isTemplate = false;
            ifType.isPointer = false;
            cc.base.add(ifType);*/
        });
        //inner classes
        for (TypeDeclaration member : node.getTypes()) {
            member.accept(this);
        }
        for (FieldDeclaration field : node.getFields()) {
            field.accept(this);
        }
        for (MethodDeclaration method : node.getMethods()) {
            method.accept(this);
        }
        stack.pop();
        return false;
    }

    /*public boolean visit(EnumDeclaration n) {
        CClass cc = new CClass();
        if (stack.size() == 0) {
            header.addClass(cc);
        }
        else {
            last().addInner(cc);
        }
        stack.push(cc);
        exprVisitor.clazz = cc;

        cc.isEnum = true;
        cc.name = n.getName().getFullyQualifiedName();
        cc.base.add(new CType("java::lang::Enum"));
        header.addInclude("java/lang/Enum");
        n.getImplementedTypes().forEach(iface -> cc.base.add(iface.accept(typeVisitor, null)));

        for (EnumConstantDeclaration constant : n.getEntries()) {
            CField cf = new CField();
            cc.addField(cf);
            cf.setPublic(true);
            cf.setStatic(true);
            cf.type = new CType(cc.name);
            cf.name = constant.getNameAsString();
            Writer rh = new Writer();
            rh.append("new ").append(cc.name);

            exprVisitor.args(constant.getArguments(), rh);
            if(constant.getBody()!=null){
                throw new RuntimeException("enum body");
            }
            cf.right = rh.toString();
        }
        if (!n.getMembers().isEmpty()) {
            n.getMembers().forEach(p -> p.accept(this, null));
        }
        stack.pop();
    }*/

    @Override
    public boolean visit(FieldDeclaration n) {

        CType type = typeVisitor.visitType(n.getType(), last());
        System.out.println("field=" + n.getType() + " resolved=" + type + " bind=" + n.getType().resolveBinding().getBinaryName());
        for (VariableDeclarationFragment frag : (List<VariableDeclarationFragment>) n.fragments()) {
            CField cf = new CField();
            last().addField(cf);
            cf.type = type;
            cf.name = frag.getName().getIdentifier();
            cf.setPublic(Modifier.isPublic(n.getModifiers()));
            cf.setStatic(Modifier.isStatic(n.getModifiers()));
            if (frag.getInitializer() != null) {
                Writer nw = new Writer();
                exprVisitor.w = nw;
                frag.getInitializer().accept(exprVisitor);
                cf.right = nw.toString();
            }
        }
        return false;
    }

    @Override
    public boolean visit(MethodDeclaration n) {
        //System.out.println("method.decl=" + n.getName());
        CMethod method = new CMethod();
        last().addMethod(method);

        n.typeParameters().forEach(temp -> method.template.add(new CType(temp.toString())));
        method.isCons = n.isConstructor();
        //type could be template
        if (!n.isConstructor()) {
            method.type = typeVisitor.visitType(n.getReturnType2(), method);
            //System.out.println("name=" + n.getName() + " type=" + method.type);
        }

        method.name = n.getName().getIdentifier();

        method.setStatic(Modifier.isStatic(n.getModifiers()));
        method.setPublic(Modifier.isPublic(n.getModifiers()));
        method.setNative(Modifier.isNative(n.getModifiers()));


        for (SingleVariableDeclaration param : (List<SingleVariableDeclaration>) n.parameters()) {
            CParameter cp = new CParameter();
            cp.type = typeVisitor.visitType(param.getType(), method);
            cp.type.isTemplate = false;
            cp.name = param.getName().getIdentifier();
            method.params.add(cp);
        }

        if (n.getBody() != null) {
            method.bodyWriter.init();
            statementVisitor.setMethod(method);
            n.getBody().accept(statementVisitor);
        }
        return false;
    }

    /*public void visit(ConstructorDeclaration n, Writer w) {
        CMethod method = new CMethod();
        last().addMethod(method);
        method.isCons = true;
        method.name = n.getNameAsString();
        method.setPublic(n.isPublic());

        if (!n.getTypeParameters().isEmpty()) {
            n.getTypeParameters().forEach(temp -> method.template.add(new CType(temp.asString())));
        }

        for (Parameter parameter : n.getParameters()) {
            CParameter cp = new CParameter();
            cp.type = typeVisitor.visitType(parameter.getType(), method);
            cp.type.isTemplate = false;
            cp.name = parameter.getNameAsString();
            method.params.add(cp);
        }
        method.bodyWriter.init();
        statementVisitor.setMethod(method);
        n.getBody().accept(statementVisitor, method.bodyWriter);
    }*/

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
