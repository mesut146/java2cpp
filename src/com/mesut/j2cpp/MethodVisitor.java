package com.mesut.j2cpp;
import com.mesut.j2cpp.parser.*;
import com.mesut.j2cpp.parser.Java8Parser.*;
import com.mesut.j2cpp.ast.*;
import java.io.*;

public class MethodVisitor extends Java8ParserBaseVisitor
{

    CMethod cm;
    Body body;
    Node tmp=new Writer();

    @Override
    public Object visitLocalVariableDeclarationStatement(Java8Parser.LocalVariableDeclarationStatementContext ctx)
    {
        body.append((String)visit(ctx.localVariableDeclaration()));
        body.append(";\n");
        return null;
    }
    
    @Override
    public Object visitLocalVariableDeclaration(Java8Parser.LocalVariableDeclarationContext ctx)
    {
        StringBuilder sb=new StringBuilder();
        String type=ctx.unannType().getText();
        for(VariableDeclaratorContext vdc:ctx.variableDeclaratorList().variableDeclarator()){
            String name=vdc.variableDeclaratorId().getText();
            //System.out.printf("type=%s name=%s rs=%s%n",type,name,right);
            sb.append(type);
            sb.append("* ").append(name);
            if(vdc.variableInitializer()!=null){
                sb.append("=");
                //Helper.debug(vdc.variableInitializer());
                sb.append(visit(vdc.variableInitializer()));
            }
            
        }
        return sb.toString();
    } 

    @Override
    public Object visitAssignment(Java8Parser.AssignmentContext ctx)
    {
        StringBuilder sb=new StringBuilder();
        String ls=(String)visitLeftHandSide(ctx.leftHandSide());
        String op=ctx.assignmentOperator().getText();
        String exp=(String)visitExpression(ctx.expression());
        sb.append(ls);
        sb.append(op);
        sb.append(exp);
        //sb.append(";\n");
        //System.out.println("as="+sb);
        //System.out.printf("ls=%s rs=%s op=%s%n",ls,exp,op);
        //body.append(sb.toString());
        
        return sb.toString();
    }

    @Override
    public Object visitExpressionName(Java8Parser.ExpressionNameContext ctx)
    {
        return ctx.Identifier().getText();
    }

    @Override
    public Object visitFieldAccess(Java8Parser.FieldAccessContext ctx)
    {
        StringBuilder sb=new StringBuilder();
        if(ctx.primary()!=null){
            sb.append(visitPrimary(ctx.primary()));
            sb.append("->");
            sb.append(ctx.Identifier().getText());
        }
        else if(ctx.typeName()!=null){
            sb.append(visitTypeName(ctx.typeName()));
            sb.append("->");
            sb.append(ctx.Identifier().getText());
        }else{//super
            sb.append(ctx.Identifier().getText());
        }
        return sb.toString();
    }

    @Override
    public Object visitFieldAccess_lf_primary(Java8Parser.FieldAccess_lf_primaryContext ctx)
    {
        return ctx.Identifier().getText();
    }

    @Override
    public Object visitFieldAccess_lfno_primary(Java8Parser.FieldAccess_lfno_primaryContext ctx)
    {
        if(ctx.typeName()!=null){
            return cm.parent.name+"::"+ctx.Identifier().getText();
        }
        else{
            //TODO
            throw new RuntimeException("todo");
        }
    }

    @Override
    public Object visitExpressionStatement(Java8Parser.ExpressionStatementContext ctx)
    {
        body.append((String)visit(ctx.statementExpression()));
        body.append(";\n");
        return null;
    }
  
    @Override
    public Object visitReturnStatement(Java8Parser.ReturnStatementContext ctx)
    {
        StringBuilder sb=new StringBuilder();
        sb.append("return");
        if(ctx.expression()!=null){
            sb.append(" ").append(visit(ctx.expression()));
        }
        sb.append(";\n");
        body.append(sb.toString());
        return null;
    }
    
    @Override
    public Object visitMethodInvocation_type1(Java8Parser.MethodInvocation_type1Context ctx)
    {
        StringBuilder sb=new StringBuilder();
        sb.append(ctx.methodName().getText()).append("(");
        if(ctx.argumentList()!=null){
            sb.append((String)visitArgumentList(ctx.argumentList()));
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public Object visitMethodInvocation_type2(Java8Parser.MethodInvocation_type2Context ctx)
    {
        StringBuilder sb=new StringBuilder();
        sb.append(ctx.typeName().getText());//meth parent
        sb.append("->");//could be pointer
        sb.append(ctx.Identifier().getText());//name
        sb.append("(");
        if(ctx.argumentList()!=null){
            sb.append((String)visitArgumentList(ctx.argumentList()));
        }
        sb.append(")");
        return sb.toString();
    }
    
    

    @Override
    public Object visitMethodInvocation_lf_primary(Java8Parser.MethodInvocation_lf_primaryContext ctx)
    {
        System.out.println("mip="+ctx.getText());
        StringBuilder sb=new StringBuilder();
        sb.append(".");
        if(ctx.typeArguments()!=null){
            
        }
        sb.append("(");
        if(ctx.argumentList()!=null){
            
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public Object visitMethodInvocation_nop_type1(Java8Parser.MethodInvocation_nop_type1Context ctx)
    {
        StringBuilder sb=new StringBuilder();
        String arg="";
        if(ctx.argumentList()!=null){
            arg=(String)visit(ctx.argumentList());
        }
        if(ctx.methodName()!=null){
            sb.append(ctx.methodName().getText());
            sb.append("(").append(arg).append(")");
        }
        return sb.toString();
    }

    @Override
    public Object visitClassInstanceCreationExpression(Java8Parser.ClassInstanceCreationExpressionContext ctx)
    {
        System.out.println("ins="+ctx.getText());
        return super.visitClassInstanceCreationExpression(ctx);
    }

    @Override
    public Object visitClassInstanceCreationExpression_lfno_primary(Java8Parser.ClassInstanceCreationExpression_lfno_primaryContext ctx)
    {
        System.out.println("nop="+ctx.getText());
        return super.visitClassInstanceCreationExpression_lfno_primary(ctx);
    }
    
    

    @Override
    public Object visitClassInstanceCreationExpression_lf_primary(Java8Parser.ClassInstanceCreationExpression_lf_primaryContext ctx)
    {
        StringBuilder sb=new StringBuilder();
        sb.append(".new");
        sb.append(ctx.Identifier().getText());
        sb.append("(");
        if(ctx.argumentList()!=null){
            sb.append(visit(ctx.argumentList()));
        }
        sb.append(")");
        if(ctx.classBody()!=null){
            throw new RuntimeException("body");
        }
        return sb.toString();
    }

    
    
    
    
    @Override
    public Object visitLiteral(Java8Parser.LiteralContext ctx)
    {
        if(ctx.NullLiteral()!=null){
            return "nullptr";
        }else if(ctx.IntegerLiteral()!=null){
            return ctx.IntegerLiteral().getText();
        }else if(ctx.StringLiteral()!=null){
            return "new String("+ctx.StringLiteral().getText()+")";
        }
        return null;
    }
    

    /*@Override
    public Object visitTypeArguments(Java8Parser.TypeArgumentsContext ctx)
    {
        // TODO: Implement this method
        return super.visitTypeArguments(ctx);
    }*/
    
    

    /*@Override
    public Object visitExpressionList(Java8Parser.ExpressionListContext ctx)
    {
        int i=0;
        for(ExpressionContext ec:ctx.expression()){
            if(i>0){
                sb.append(",");
            }
            sb.append(visitExpression(ec));
            i++;
        }
        
        return sb.toString();
    }*/

    
    
    
    
    
    
    
    
    
}
