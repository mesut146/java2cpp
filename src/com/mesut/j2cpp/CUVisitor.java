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
        //System.out.println("pkg");
        return null;
    }

    @Override
    public Object visitImportDeclaration(JavaParser.ImportDeclarationContext ctx) {
        //System.out.println("imp");
        //writer.println("include \""+str+"\"");
        return null;
    }

    @Override
    public Object visitTypeDeclaration(JavaParser.TypeDeclarationContext ctx) {
        //System.out.println("type="+ctx.classDeclaration().toStringTree());

        this.visit(ctx.classDeclaration());
        //visitClassDeclaration(ctx.classDeclaration());
        return null;
    }

    @Override
    public Object visitClassDeclaration(JavaParser.ClassDeclarationContext ctx) {
        writer.print("class ");
        writer.print(ctx.IDENTIFIER().getText());
        writer.println("{");
        
        visit(ctx.classBody().classBodyDeclaration(0).memberDeclaration().fieldDeclaration());
        writer.println("};");
        //System.out.println("asasa");
        //writer.write("test");
        
        return null;
    }

    @Override
    public Object visitFieldDeclaration(JavaParser.FieldDeclarationContext ctx)
    {
        String type=ctx.typeType().getText();
        if(Helper.is(type)){
            String tc=Helper.getType(type);
            writer.print(tc);
            writer.print(" ");
        }else{
            
        }
        
        writer.println(";");
        return null;
    }
    
    


}
