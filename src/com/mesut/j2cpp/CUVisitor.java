package com.mesut.j2cpp;

import com.mesut.j2cpp.parser.JavaParser;
import com.mesut.j2cpp.parser.JavaParserBaseVisitor;
import com.mesut.j2cpp.parser.JavaParserVisitor;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.io.PrintWriter;
import com.mesut.j2cpp.parser.JavaParser.*;

public class CUVisitor extends JavaParserBaseVisitor {

    PrintWriter writer;


    @Override
    public Object visitPackageDeclaration(JavaParser.PackageDeclarationContext ctx) {
        System.out.println("pkg");
        //namespace
        return null;
    }

    @Override
    public Object visitImportDeclaration(JavaParser.ImportDeclarationContext ctx) {
        System.out.println("imp");
        //writer.println("include \""+str+"\"");
        return null;
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
        writer.print("class ");
        writer.print(ctx.IDENTIFIER().getText());
        writer.println("{");
        
        if(ctx.typeType()!=null){
            
        }
        if(ctx.typeList()!=null){
            
        }
        for(ClassBodyDeclarationContext cbdc:ctx.classBody().classBodyDeclaration()){
            visitChildren(cbdc);
        }
        //visit(ctx.classBody().classBodyDeclaration(0).memberDeclaration().fieldDeclaration());
        writer.println("}");
        //System.out.println("asasa");
        //writer.write("test");
        
        return null;
    }

    @Override
    public Object visitClassBodyDeclaration(JavaParser.ClassBodyDeclarationContext ctx)
    {
        if(ctx.STATIC()!=null){
            //TODO
        }
        visit(ctx.memberDeclaration());
        return super.visitClassBodyDeclaration(ctx);
    }

    @Override
    public Object visitMemberDeclaration(JavaParser.MemberDeclarationContext ctx)
    {
        // TODO: Implement this method
        System.out.println("mem="+ctx.getText());
        return super.visitMemberDeclaration(ctx);
    }
    
    

    @Override
    public Object visitFieldDeclaration(JavaParser.FieldDeclarationContext ctx)
    {
        String jtype=ctx.typeType().getText();
        
        if(Helper.is(jtype)){
            String ctype=Helper.getType(jtype);
            writer.print(ctype);
            writer.print(" ");
            writer.print(ctx.variableDeclarators().getText());
            
            
            
        }else{
            System.out.println("else="+jtype);
        }
        
        writer.println(";");
        return null;
    }
    
    


}
