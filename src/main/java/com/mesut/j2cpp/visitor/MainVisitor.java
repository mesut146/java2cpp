package com.mesut.j2cpp.visitor;

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.mesut.j2cpp.Converter;
import com.mesut.j2cpp.Writer;
import com.mesut.j2cpp.ast.*;

import java.util.Stack;

//visitor for single compilation unit
//fields,method decl,class decl
public class MainVisitor extends VoidVisitorAdapter<Writer> {

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

    public void visit(PackageDeclaration n, Writer arg) {
        Namespace ns = new Namespace();
        ns.pkg(n.getNameAsString());
        header.ns = ns;
    }

    public void visit(ImportDeclaration n, Writer w) {
        String imp = n.getNameAsString();
        imp = imp.replace(".", "/");
        if (n.isStatic()) {
            //base.cls.var;
            int idx = imp.lastIndexOf("/");
            if (idx != -1) {
                imp = imp.substring(0, idx - 1);
            }
            header.includes.add(imp);
        }
        if (n.isAsterisk()) {
            //TODO
            //resolve seperately
            header.importStar.add(imp);
        } else {
            header.includes.add(imp);
        }

    }


    public void visit(ClassOrInterfaceDeclaration n, Writer s) {
        CClass cc = new CClass();
        if (stack.size() == 0) {
            header.addClass(cc);
        } else {
            last().addInner(cc);
        }
        stack.push(cc);

        exprVisitor.clazz = cc;
        cc.name = n.getNameAsString();
        cc.isInterface = n.isInterface();
        n.getTypeParameters().forEach(type -> cc.template.add(new CType(type.getNameAsString(), true)));
        n.getExtendedTypes().forEach(base -> {
            CType baseType = typeVisitor.visitType(base, cc);
            baseType.isTemplate = false;
            baseType.isPointer = false;
            cc.base.add(baseType);
        });
        n.getImplementedTypes().forEach(iface -> {
            CType ifType = typeVisitor.visitType(iface, cc);
            ifType.isTemplate = false;
            ifType.isPointer = false;
            cc.base.add(ifType);
        });
        n.getMembers().forEach(p -> p.accept(this, null));
        stack.pop();
    }

    public void visit(EnumDeclaration n, Writer w) {
        CClass cc = new CClass();
        if (stack.size() == 0) {
            header.addClass(cc);
        } else {
            last().addInner(cc);
        }
        stack.push(cc);
        exprVisitor.clazz = cc;

        cc.isEnum = true;
        cc.name = n.getNameAsString();
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
            /*if(constant.getBody()!=null){
                throw new RuntimeException("enum body");
            }*/
            cf.right = rh.toString();
        }
        if (!n.getMembers().isEmpty()) {
            n.getMembers().forEach(p -> p.accept(this, null));
        }
        stack.pop();
    }

    public void visit(FieldDeclaration n, Writer s) {
        for (VariableDeclarator vd : n.getVariables()) {
            CField cf = new CField();
            last().addField(cf);
            cf.type = typeVisitor.visitType(vd.getType(), last());
            //cf.type.arrayLevel=vd.getType().getArrayLevel();
            cf.name = vd.getNameAsString();
            cf.setStatic(n.isStatic());
            cf.setPublic(n.isPublic());

            if (vd.getInitializer().isPresent()) {
                Writer nw = new Writer();
                vd.getInitializer().get().accept(exprVisitor, nw);
                cf.right = nw.toString();
            }
        }
    }

    public void visit(MethodDeclaration n, Writer w) {
        CMethod method = new CMethod();
        last().addMethod(method);

        n.getTypeParameters().forEach(temp -> method.template.add(new CType(temp.getNameAsString())));
        //type could be template
        method.type = typeVisitor.visitType(n.getType(), method);
        method.name = n.getName().asString();
        method.setStatic(n.isStatic());
        method.setPublic(n.isPublic());
        method.setNative(n.isNative());

        for (Parameter parameter : n.getParameters()) {
            CParameter cp = new CParameter();
            cp.type = typeVisitor.visitType(parameter.getType(), method);
            cp.type.isTemplate = false;
            cp.name = parameter.getNameAsString();
            method.params.add(cp);
        }
        if (n.getBody().isPresent()) {
            method.body.init();
            statementVisitor.setMethod(method);
            n.getBody().get().accept(statementVisitor, method.body);
        }
    }

    public void visit(ConstructorDeclaration n, Writer w) {
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
        method.body.init();
        statementVisitor.setMethod(method);
        n.getBody().accept(statementVisitor, method.body);
    }

    //static block
    @Override
    public void visit(InitializerDeclaration n, Writer w) {
        if (n.isStatic()) {
            header.includePath("static_block.hpp");
            w = new Writer();
            last().staticBlock = w;
            w.append("static_block");
            n.getBody().accept(statementVisitor, w);
        }
    }
}
