package com.mesut.j2cpp;
import com.mesut.j2cpp.parser.*;
import com.mesut.j2cpp.parser.JavaParser.*;
import com.mesut.j2cpp.ast.*;

public class MethodVisitor extends JavaParserBaseVisitor
{

    CMethod cm;
    
    @Override
    public Object visitMethodBody(JavaParser.MethodBodyContext ctx)
    {
        // TODO: Implement this method
        visitBlock(ctx.block());
        return null;
    }

    @Override
    public Object visitBlock(JavaParser.BlockContext ctx)
    {
        for(BlockStatementContext bsc:ctx.blockStatement()){
            visitBlockStatement(bsc);
        }
        return null;
    }

    /*@Override
    public Object visitBlockStatement(JavaParser.BlockStatementContext ctx)
    {
        return null;
    }*/

    @Override
    public Object visitLocalVariableDeclaration(JavaParser.LocalVariableDeclarationContext ctx)
    {
        String type=ctx.typeType().getText();
        for(VariableDeclaratorContext vdc:ctx.variableDeclarators().variableDeclarator()){
            String name=vdc.variableDeclaratorId().getText();
            String right=vdc.variableInitializer().getText();
            //System.out.printf("type=%s name=%s rs=%s%n",type,name,right);
            cm.body.print(type);
            cm.body.append(" ").append(name);
            cm.body.append(";");
        }
        return null;
    }
    
    
    
    
    
}
