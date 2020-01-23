package com.mesut.j2cpp.visitor;

import com.github.javaparser.ast.expr.InstanceOfExpr;
import com.github.javaparser.ast.visitor.*;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.*;
import com.mesut.j2cpp.*;
import com.mesut.j2cpp.ast.*;

import java.util.*;

public class MainVisitor extends VoidVisitorAdapter<Nodew> {

    public CHeader header;
    public MethodVisitor mv;
    public Stack<CClass> stack = new Stack<>();
    public Converter converter;

    public MainVisitor(Converter converter,CHeader header) {
        this.converter = converter;
        this.header=header;
        this.mv=new MethodVisitor(converter,header);
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
            header.includes.add(imp + ".h");
        }
        if (n.isAsterisk()) {
            //TODO
            //resolve seperately
            header.importStar.add(imp);
        } else {
            header.includes.add(imp + ".h");
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
        n.getTypeParameters().forEach(type-> cc.template.add((CType) type.accept(mv,new Nodew())));
        n.getExtendedTypes().forEach(ex -> cc.base.add((CType)ex.accept(mv,new Nodew())));
        n.getImplementedTypes().forEach(iface -> cc.base.add((CType)iface.accept(mv,new Nodew())));
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
        cc.base.add(new CType("Enum"));
        n.getImplementedTypes().forEach(iface -> cc.base.add(new CType(iface.getNameAsString())));
        for (EnumConstantDeclaration ec : n.getEntries()) {
            CField cf = new CField();
            cc.addField(cf);
            cf.setPublic(true);
            cf.setStatic(true);
            cf.type = new CType(cc.name);
            cf.name = ec.getNameAsString();
            Nodew rh = new Nodew();
            rh.append("new ").append(cc.name);

            mv.args(ec.getArguments(), rh);
            /*if(ec.getBody()!=null){
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
            cf.type = (CType) vd.getType().accept(mv,new Nodew());
            //cf.type.arrayLevel=vd.getType().getArrayLevel();
            cf.name = vd.getNameAsString();
            cf.setStatic(n.isStatic());
            cf.setPublic(n.isPublic());

            if (vd.getInitializer().isPresent()) {
                Nodew nw = new Nodew();
                vd.getInitializer().get().accept(mv, nw);
                cf.right = nw.toString();
            }
        }
    }

    public void visit(MethodDeclaration n, Nodew w) {
        CMethod cm = new CMethod();
        last().addMethod(cm);
        cm.type = (CType) n.getType().accept(mv,new Nodew());
        cm.name = n.getName().asString();
        cm.setStatic(n.isStatic());
        cm.setPublic(n.isPublic());
        cm.setNative(n.isNative());

        for (Parameter p : n.getParameters()) {
            CParameter cp = new CParameter();
            cp.type = (CType) p.getType().accept(mv,new Nodew());
            cp.name = p.getNameAsString();
            cm.params.add(cp);
        }
        if (n.getBody().isPresent()) {
            cm.body.init();
            mv.method = cm;
            n.getBody().get().accept(mv, cm.body);
        }
    }

    public void visit(ConstructorDeclaration n, Nodew w) {
        CMethod cm = new CMethod();
        last().addMethod(cm);
        cm.isCons = true;
        cm.name = n.getNameAsString();
        cm.setPublic(n.isPublic());
        for (Parameter p : n.getParameters()) {
            CParameter cp = new CParameter();
            cp.type = (CType) p.getType().accept(mv,new Nodew());
            cp.name = p.getNameAsString();
            cm.params.add(cp);
        }
        cm.body.init();
        mv.method = cm;
        n.getBody().accept(mv, cm.body);
    }

    @Override
    public void visit(InitializerDeclaration n, Nodew w) {
        if (n.isStatic()){
            header.addRuntime();
            w=new Nodew();
            last().staticBlock=w;
            w.append("static_block");
            n.getBody().accept(mv,w);
        }
    }
}
