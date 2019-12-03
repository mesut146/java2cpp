package com.mesut.j2cpp.visitor;
import com.github.javaparser.ast.visitor.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.modules.*;
import com.github.javaparser.ast.type.*;
import com.github.javaparser.ast.comments.*;
import com.mesut.j2cpp.*;
import com.mesut.j2cpp.ast.*;
import java.util.*;

public class MainVisitor extends VoidVisitorAdapter<Nodew>
{

    public CHeader h;
    public Stack<CClass> stack=new Stack<>();
    
    public CClass last(){
        return stack.peek();
    }
    
    public void visit(PackageDeclaration n, Nodew arg) {
        Namespace ns=new Namespace();
        ns.pkg(n.getNameAsString());
        h.ns=ns;
    }
    
    public void visit(ImportDeclaration n,Nodew w){
        String imp=n.getNameAsString();
        imp=imp.replace(".","::");
        if(n.isStatic()){
            //base.cls.var;
            int idx=imp.lastIndexOf("::");
            if(idx!=-1){
                imp=imp.substring(0,idx-1);
            }
            h.includes.add(imp+".h");
        }
        if(n.isAsterisk()){
            //TODO
            //resolve seperately
        }else{
            h.includes.add(imp+".h");
        }
        
    }
    
    public void visit(ClassOrInterfaceDeclaration n,Nodew s){
        CClass cc=new CClass();
        if(stack.size()==0){
            h.addClass(cc);
        }else{
            last().addInner(cc);
        }
        stack.push(cc);
        cc.name=n.getName().asString();
        cc.isInterface=n.isInterface();
        n.getExtendedTypes().forEach(ex->cc.base.add(new TypeName(ex.getNameAsString())));
        n.getImplementedTypes().forEach(iface->cc.base.add(new TypeName(iface.getNameAsString())));
        n.getMembers().forEach(p->p.accept(this,null));
        stack.pop();
    }

    public void visit(EnumDeclaration n,Nodew w){

    }
    
    public void visit(FieldDeclaration n,Nodew s){
        
        for(VariableDeclarator vd:n.getVariables()){
            
            CField cf=new CField();
            last().addField(cf);
            cf.type=new TypeName(vd.getType().asString());
            cf.name=vd.getName().asString();
            cf.isStatic=n.isStatic();
            if(vd.getInitializer().isPresent()){
                Nodew nw=new Nodew();
                vd.getInitializer().get().accept(new MethodVisitor(),nw);
                cf.right=nw.toString();
            }
            
        }
    }
    
    public void visit(MethodDeclaration n,Nodew w){
        CMethod cm=new CMethod();
        last().addMethod(cm);
        cm.type=new TypeName(n.getType().asString());
        cm.name=n.getName().asString();
        if(n.isStatic()){
            cm.modifiers.add("static");
        }
        
        for(Parameter p:n.getParameters()){
            CParameter cp=new CParameter();
            cp.type=new TypeName(p.getTypeAsString());
            cp.name=p.getNameAsString();
            cm.params.add(cp);
        }
        if(n.getBody().isPresent()){
            //cm.body.level=1;
            cm.body.init();
            MethodVisitor mv=new MethodVisitor();
            n.getBody().get().accept(mv,cm.body);
        }
    }
    
    public void visit(ConstructorDeclaration n,Nodew w){
        CMethod cm=new CMethod();
        last().addMethod(cm);
        cm.isCons=true;
        cm.name=n.getName().asString();

        for(Parameter p:n.getParameters()){
            CParameter cp=new CParameter();
            cp.type=new TypeName(p.getTypeAsString());
            cp.name=p.getNameAsString();
            cm.params.add(cp);
        }
        //cm.body.level=1;
        cm.body.init();
        MethodVisitor mv=new MethodVisitor();
        //mv.body=cm.body;
        n.getBody().accept(mv,cm.body);
    }
    
}
