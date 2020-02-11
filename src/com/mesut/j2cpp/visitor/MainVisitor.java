package com.mesut.j2cpp.visitor;

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.mesut.j2cpp.Converter;
import com.mesut.j2cpp.Nodew;
import com.mesut.j2cpp.ast.*;

import java.util.Stack;

//fields,method decl,class decl
public class MainVisitor extends VoidVisitorAdapter<Nodew> {

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
        //this.methodVisitor = new MethodVisitor(converter, header);
        this.typeVisitor = new TypeVisitor(converter, header);
        this.exprVisitor = new ExprVisitor(converter, header, typeVisitor);
        this.statementVisitor = new StatementVisitor(converter, header, exprVisitor, typeVisitor);
    }

    public CClass last() {
        return stack.peek();
    }

    public void visit(PackageDeclaration n, Nodew arg) {
        Namespace ns = new Namespace();
        ns.pkg(n.getNameAsString());
        header.ns = ns;
    }

    public void visit(ImportDeclaration n, Nodew w) {
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

    public void visit(ClassOrInterfaceDeclaration n, Nodew s) {
        //System.out.println("class="+n.getFullyQualifiedName().get());
        CClass cc = new CClass();
        if (stack.size() == 0) {
            header.addClass(cc);
        } else {
            last().addInner(cc);
        }
        stack.push(cc);

        cc.name = n.getNameAsString();
        cc.isInterface = n.isInterface();
        n.getTypeParameters().forEach(type -> cc.template.add((CType) type.accept(typeVisitor, null)));
        //System.out.println("temp=" + cc.getTemplate().getList());
        //n.getTypeParameters().forEach(type -> System.out.println(type.isReferenceType()));
        n.getExtendedTypes().forEach(ex -> cc.base.add((CType) ex.accept(typeVisitor, null)));
        n.getImplementedTypes().forEach(iface -> cc.base.add((CType) iface.accept(typeVisitor, null)));
        n.getMembers().forEach(p -> p.accept(this, null));
        stack.pop();
    }

    public void visit(EnumDeclaration n, Nodew w) {
        CClass cc = new CClass();
        if (stack.size() == 0) {
            header.addClass(cc);
        } else {
            last().addInner(cc);
        }
        stack.push(cc);
        cc.isEnum = true;
        cc.name = n.getNameAsString();
        cc.base.add(new CType("java::lang::Enum"));
        header.addInclude("java/lang/Enum");
        n.getImplementedTypes().forEach(iface -> cc.base.add((CType) iface.accept(typeVisitor, new Nodew())));

        for (EnumConstantDeclaration constant : n.getEntries()) {
            CField cf = new CField();
            cc.addField(cf);
            cf.setPublic(true);
            cf.setStatic(true);
            cf.type = new CType(cc.name);
            cf.name = constant.getNameAsString();
            Nodew rh = new Nodew();
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

    public void visit(FieldDeclaration n, Nodew s) {
        for (VariableDeclarator vd : n.getVariables()) {
            CField cf = new CField();
            last().addField(cf);
            cf.type = typeVisitor.visitType(vd.getType(), last());
            //cf.type.arrayLevel=vd.getType().getArrayLevel();
            cf.name = vd.getNameAsString();
            cf.setStatic(n.isStatic());
            cf.setPublic(n.isPublic());

            if (vd.getInitializer().isPresent()) {
                Nodew nw = new Nodew();
                vd.getInitializer().get().accept(exprVisitor, nw);
                cf.right = nw.toString();
            }
        }
    }

    public void visit(MethodDeclaration n, Nodew w) {
        CMethod method = new CMethod();
        last().addMethod(method);
        if (!n.getTypeParameters().isEmpty()) {
            n.getTypeParameters().forEach(temp -> method.template.add(new CType(temp.asString())));
        }
        //type could be template
        method.type = typeVisitor.visitType(n.getType(), method);

        method.name = n.getName().asString();
        method.setStatic(n.isStatic());
        method.setPublic(n.isPublic());
        method.setNative(n.isNative());

        for (Parameter parameter : n.getParameters()) {
            CParameter cp = new CParameter();
            cp.type = typeVisitor.visitType(parameter.getType(), method);
            cp.name = parameter.getNameAsString();
            method.params.add(cp);
        }
        if (n.getBody().isPresent()) {
            method.body.init();
            statementVisitor.setMethod(method);
            n.getBody().get().accept(statementVisitor, method.body);
        }
    }

    public void visit(ConstructorDeclaration n, Nodew w) {
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
            cp.name = parameter.getNameAsString();
            method.params.add(cp);
        }
        method.body.init();
        statementVisitor.method = method;
        n.getBody().accept(statementVisitor, method.body);
    }

    //static block
    @Override
    public void visit(InitializerDeclaration n, Nodew w) {
        if (n.isStatic()) {
            header.addRuntime();
            w = new Nodew();
            last().staticBlock = w;
            w.append("static_block");
            n.getBody().accept(statementVisitor, w);
        }
    }
}
