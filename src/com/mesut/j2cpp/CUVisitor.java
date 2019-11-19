package com.mesut.j2cpp;

import com.mesut.j2cpp.parser.*;
import org.antlr.v4.runtime.tree.*;

import java.io.PrintWriter;
import com.mesut.j2cpp.parser.JavaParser.*;
import com.mesut.j2cpp.ast.*;
import java.util.*;

public class CUVisitor extends JavaParserBaseVisitor {

    CHeader h;
    Stack<CClass> stack=new Stack<>();
    
    @Override
    public Object visitPackageDeclaration(JavaParser.PackageDeclarationContext ctx) {
        //System.out.println("pkg");
        //namespace
        Namespace ns=new Namespace();
        ns.pkg(ctx.qualifiedName().getText());
        h.ns=ns;
        return ns;
    }

    @Override
    public Object visitImportDeclaration(JavaParser.ImportDeclarationContext ctx) {
        //System.out.println("imp="+ctx.getText());
        //writer.println("include \""+str+"\"");
        if(ctx.MUL()!=null){
            //TODO
        }
        h.includes.add(ctx.qualifiedName().getText().replace(".","/"));
        return null;
    }
    
    CClass last(){
        return stack.peek();
    }

    /*@Override
    public Object visitTypeDeclaration(JavaParser.TypeDeclarationContext ctx) {
        //System.out.println("type="+ctx.classDeclaration().toStringTree());
        if(ctx.classDeclaration()!=null){
            visit(ctx.classDeclaration());
        }else if(ctx.interfaceDeclaration()!=null){
            
        }
        //visitClassDeclaration(ctx.classDeclaration());
        return null;
    }*/

    
    @Override
    public Object visitClassDeclaration(JavaParser.ClassDeclarationContext ctx) {

        //System.out.println("class dec");
        CClass cc=new CClass();
        cc.isInterface=false;
        //last=cc;
        if(stack.size()==0){
            stack.push(cc);
            h.addClass(cc);
        }else{
            last().addInner(cc);
            stack.push(cc);
        }
        
        cc.name=ctx.IDENTIFIER().getText();
        
        if(ctx.typeType()!=null){//extends
            cc.base.add(ctx.typeType().getText());
        }
        
        if(ctx.typeList()!=null){
            typeList(ctx.typeList(),cc);
        }
        for(ClassBodyDeclarationContext cbdc:ctx.classBody().classBodyDeclaration()){
            visitClassBodyDeclaration(cbdc);
        }
        //System.out.println("end class dec");
        stack.pop();
        return cc;
    }

    public Object visitClassBodyDeclaration(JavaParser.ClassBodyDeclarationContext ctx)
    {
        if(ctx.STATIC()!=null){
            //TODO
        }
        visit(ctx.memberDeclaration());
        return null;
    }

    @Override
    public Object visitInterfaceDeclaration(JavaParser.InterfaceDeclarationContext ctx)
    {
        //System.out.println("iface dec");
        CClass cc=new CClass();
        //last=cc;
        if(stack.size()==0){
            stack.push(cc);
            h.addClass(cc);
        }else{
            last().addInner(cc);
            stack.push(cc);
        }
        cc.name=ctx.IDENTIFIER().getText();
        cc.isInterface=true;
        if(ctx.typeList()!=null){
            typeList(ctx.typeList(),cc);
        }
        visit(ctx.interfaceBody());
        stack.pop();
        return cc;
    }

    //iface field
    @Override
    public Object visitConstDeclaration(JavaParser.ConstDeclarationContext ctx)
    {
        //System.out.println("const dec="+ctx.getText());
        for(ConstantDeclaratorContext cdc:ctx.constantDeclarator()){
            CField cf=new CField();
            cf.type=ctx.typeType().getText();
            cf.name=cdc.IDENTIFIER().getText();
            cf.right=cdc.variableInitializer().getText();
            last().addField(cf);
            
        }
        return null;
    }

    @Override
    public Object visitInterfaceMethodDeclaration(JavaParser.InterfaceMethodDeclarationContext ctx)
    {
        //System.out.println("iface meth");
        CMethod cm=new CMethod();
        cm.empty=true;
        last().methods.add(cm);
        for(InterfaceMethodModifierContext immc:ctx.interfaceMethodModifier()){
            cm.modifiers.add(immc.getText());
        }
        cm.type=ctx.typeTypeOrVoid().getText();
        cm.name=ctx.IDENTIFIER().getText();
        param(ctx.formalParameters(),cm);
        
        visit(ctx.methodBody());
        return cm;
    }
    
    void param(FormalParametersContext fpc,CMethod cm){
        FormalParameterListContext fplc=fpc.formalParameterList();
        if(fplc!=null){
            for(FormalParameterContext fpc2:fplc.formalParameter()){
                CParameter cp=new CParameter();
                cp.type=fpc2.typeType().getText();
                if(Helper.is(cp.type)){
                    cp.isPointer=false;
                }
                cp.name=fpc2.variableDeclaratorId().getText();
                cm.params.add(cp);

            }
            LastFormalParameterContext lfpc=fplc.lastFormalParameter();
            if(lfpc!=null){
                //TODO could be array
                CParameter cp=new CParameter();
                cp.type=lfpc.typeType().getText();
                cp.name=lfpc.variableDeclaratorId().getText();
                cm.params.add(cp);
            }
        }
    }

    void typeList(TypeListContext ctx,CClass cc){
        for(TypeTypeContext ttc:ctx.typeType()){
            cc.base.add(ttc.getText());
        }
    }
    
    /*public Object visitMemberDeclaration(JavaParser.MemberDeclarationContext ctx,CClass cc)
    {
        // TODO: Implement this method
        //System.out.println("mem="+ctx.getText());
        //return super.visitMemberDeclaration(ctx);
        
        return null;
    }*/
    
    
    @Override
    public Object visitFieldDeclaration(JavaParser.FieldDeclarationContext ctx)
    {
        //System.out.println("field");
        String jtype=ctx.typeType().getText();
        
        String ctype;
        if(Helper.is(jtype)){
            ctype=Helper.getType(jtype);
            
        }else{
            ctype=jtype;
        }
        for(VariableDeclaratorContext vdc:ctx.variableDeclarators().variableDeclarator()){
            CField cf=new CField();
            cf.name=vdc.variableDeclaratorId().getText();
            cf.type=ctype;
            cf.isPublic=true;
            last().addField(cf);
        }
        return null;
    }

    @Override
    public Object visitMethodDeclaration(JavaParser.MethodDeclarationContext ctx)
    {
        CMethod cm=new CMethod();
        last().addMethod(cm);
        cm.type=ctx.typeTypeOrVoid().getText();
        cm.name=ctx.IDENTIFIER().getText();
        param(ctx.formalParameters(),cm);
        //visit(ctx.methodBody());
        MethodVisitor mv=new MethodVisitor();
        mv.cm=cm;
        //cm.visitor=mv;
        mv.visitMethodBody(ctx.methodBody());
        return cm;
    }

    @Override
    public Object visitConstructorDeclaration(JavaParser.ConstructorDeclarationContext ctx)
    {
        CMethod cm=new CMethod();
        last().addMethod(cm);
        cm.name=ctx.IDENTIFIER().getText();
        cm.isCons=true;
        param(ctx.formalParameters(),cm);
        if(ctx.qualifiedNameList()!=null){
            
        }
        MethodVisitor mv=new MethodVisitor();
        mv.cm=cm;
        //cm.visitor=mv;
        mv.visitBlock(ctx.block());
        return cm;
    }
    
    


}
