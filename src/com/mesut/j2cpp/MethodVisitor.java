package com.mesut.j2cpp;
import com.mesut.j2cpp.parser.*;
import com.mesut.j2cpp.parser.Java8Parser.*;
import com.mesut.j2cpp.ast.*;
import java.io.*;
import java.util.*;
import org.antlr.v4.runtime.tree.*;

public class MethodVisitor extends Java8ParserBaseVisitor
{

    CMethod cm;
    Body body;
    //Node tmp=new FWriter();

    @Override
    public Object visitLocalVariableDeclarationStatement(Java8Parser.LocalVariableDeclarationStatementContext ctx)
    {
        return (String)visit(ctx.localVariableDeclaration()) + ";";
    }

    @Override
    public Object visitLocalVariableDeclaration(Java8Parser.LocalVariableDeclarationContext ctx)
    {
        StringBuilder sb=new StringBuilder();
        TypeName type=new TypeName(ctx.unannType().getText());
        for (VariableDeclaratorContext vdc:ctx.variableDeclaratorList().variableDeclarator())
        {
            String name=vdc.variableDeclaratorId().getText();
            //System.out.printf("type=%s name=%s rs=%s%n",type,name,right);
            sb.append(type);
            if(!type.isArray()){
                sb.append("*");
            }
            sb.append(" ").append(name);
            if (vdc.variableInitializer() != null)
            {
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
        if (ctx.primary() != null)
        {
            //System.out.println("pri="+ctx.primary().getText());
            sb.append(visitPrimary(ctx.primary()));
            sb.append("->");
            sb.append(ctx.Identifier().getText());
        }
        else if (ctx.typeName() != null)
        {
            sb.append(visitTypeName(ctx.typeName()));
            sb.append("->");
            sb.append(ctx.Identifier().getText());
        }
        else
        {//super
            sb.append(ctx.Identifier().getText());
        }
        return sb.toString();
    }

    @Override
    public Object visitFieldAccess_lf_primary(Java8Parser.FieldAccess_lf_primaryContext ctx)
    {
        return "->" + ctx.Identifier().getText();
    }

    @Override
    public Object visitFieldAccess_lfno_primary(Java8Parser.FieldAccess_lfno_primaryContext ctx)
    {
        if (ctx.typeName() != null)
        {
            return cm.parent.name + "::" + ctx.Identifier().getText();
        }
        else
        {
            //TODO
            throw new RuntimeException("todo");
        }
    }

    @Override
    public Object visitExpressionStatement(Java8Parser.ExpressionStatementContext ctx)
    {
        return (String)visitStatementExpression(ctx.statementExpression()) + ";";
    }

    @Override
    public Object visitReturnStatement(Java8Parser.ReturnStatementContext ctx)
    {
        StringBuilder sb=new StringBuilder();
        sb.append("return");
        if (ctx.expression() != null)
        {
            sb.append(" ").
                append(visitExpression(ctx.expression()));
        }
        sb.append(";");
        //body.append(sb.toString());
        return sb.toString();
    }

    @Override
    public Object visitMethodInvocation_type1(Java8Parser.MethodInvocation_type1Context ctx)
    {
        StringBuilder sb=new StringBuilder();
        sb.append(ctx.methodName().getText()).append("(");
        arg(sb, ctx.argumentList());
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
        arg(sb, ctx.argumentList());
        sb.append(")");
        return sb.toString();
    }

    @Override
    public Object visitMethodInvocation_type3(Java8Parser.MethodInvocation_type3Context ctx)
    {
        StringBuilder sb=new StringBuilder();
        sb.append(visit(ctx.expressionName()));//meth parent
        sb.append("->");//could be pointer
        sb.append(ctx.Identifier().getText());//name
        sb.append("(");
        arg(sb, ctx.argumentList());
        sb.append(")");
        return sb.toString();
    }

    @Override
    public Object visitMethodInvocation_type4(Java8Parser.MethodInvocation_type4Context ctx)
    {
        StringBuilder sb=new StringBuilder();
        sb.append(visitPrimary(ctx.primary()));//meth parent
        sb.append("->");//could be pointer
        sb.append(ctx.Identifier().getText());//name
        sb.append("(");
        arg(sb, ctx.argumentList());
        sb.append(")");
        return sb.toString();
    }

    @Override
    public Object visitMethodInvocation_type5(Java8Parser.MethodInvocation_type5Context ctx)
    {
        StringBuilder sb=new StringBuilder();
        if (true) throw new RuntimeException("type5");
        sb.append("->");//could be pointer
        sb.append(ctx.Identifier().getText());//name
        sb.append("(");
        arg(sb, ctx.argumentList());
        sb.append(")");
        return sb.toString();
    }

    @Override
    public Object visitMethodInvocation_type6(Java8Parser.MethodInvocation_type6Context ctx)
    {
        StringBuilder sb=new StringBuilder();
        if (true)
        {
            throw new RuntimeException("type6");
        }
        sb.append(visit(ctx.typeName()));//meth parent
        sb.append("->");//could be pointer
        sb.append(ctx.Identifier().getText());//name
        sb.append("(");
        arg(sb, ctx.argumentList());
        sb.append(")");
        return sb.toString();
    }

    @Override
    public Object visitMethodInvocation_lf_primary(Java8Parser.MethodInvocation_lf_primaryContext ctx)
    {
        //System.out.println("mip=" + ctx.getText());
        StringBuilder sb=new StringBuilder();
        sb.append("->");
        if (ctx.typeArguments() != null)
        {

        }
        sb.append(ctx.Identifier().getText());
        sb.append("(");
        arg(sb, ctx.argumentList());
        sb.append(")");
        return sb.toString();
    }


    @Override
    public Object visitMethodInvocation_nop_type1(Java8Parser.MethodInvocation_nop_type1Context ctx)
    {
        //System.out.println("nop1="+ctx.getText());
        
        StringBuilder sb=new StringBuilder();
        sb.append(ctx.methodName().getText());
        sb.append("(");
        arg(sb, ctx.argumentList());
        sb.append(")");
        return sb.toString();
    }

    @Override
    public Object visitMethodInvocation_nop_type2(Java8Parser.MethodInvocation_nop_type2Context ctx)
    {
        StringBuilder sb=new StringBuilder();
        //System.out.println("nop2="+ctx.getText());
        sb.append(ctx.typeName().getText());
        sb.append("->");
        sb.append(ctx.Identifier().getText());
        sb.append("(");
        arg(sb, ctx.argumentList());
        sb.append(")");
        return sb.toString();
    }

    @Override
    public Object visitMethodInvocation_nop_type3(Java8Parser.MethodInvocation_nop_type3Context ctx)
    {
        StringBuilder sb=new StringBuilder();
        sb.append(ctx.expressionName().getText());
        sb.append("->");
        sb.append(ctx.Identifier().getText());
        sb.append("(");
        arg(sb, ctx.argumentList());
        sb.append(")");
        return sb.toString();
    }

    @Override
    public Object visitMethodInvocation_nop_type4(Java8Parser.MethodInvocation_nop_type4Context ctx)
    {
        StringBuilder sb=new StringBuilder();
        if (true)
            throw new RuntimeException("super");
        sb.append("->");
        sb.append(ctx.Identifier().getText());
        sb.append("(");
        arg(sb, ctx.argumentList());
        sb.append(")");
        return sb.toString();
    }

    @Override
    public Object visitMethodInvocation_nop_type5(Java8Parser.MethodInvocation_nop_type5Context ctx)
    {
        StringBuilder sb=new StringBuilder();
        if (true)
            throw new RuntimeException("super");
        sb.append("->");
        sb.append(ctx.Identifier().getText());
        sb.append("(");
        arg(sb, ctx.argumentList());
        sb.append(")");
        return sb.toString();
    }

    /*@Override
    public Object visitMethodInvocation_lfno_primary(Java8Parser.MethodInvocation_lfno_primaryContext ctx)
    {
        //System.out.println("ifnop="+ctx.getText());
        return super.visitMethodInvocation_lfno_primary(ctx);
    }*/
    void arg(StringBuilder sb, ArgumentListContext ctx)
    {
        if (ctx != null)
        {
            sb.append(visitArgumentList(ctx));
        }
    }
    
    
    @Override
    public Object visitPrimaryNoNewArray(Java8Parser.PrimaryNoNewArrayContext ctx)
    {
        if (ctx.VOID() != null)
        {
            return "Void.class";
        }
        if (ctx.THIS() != null)
        {
            return "this";
        }
        if (ctx.typeName() != null && ctx.DOT() != null && ctx.THIS() != null)
        {
            return visitTypeName(ctx.typeName()) + "->this";
        }
        return super.visitPrimaryNoNewArray(ctx);
    }

    @Override
    public Object visitPrimaryNoNewArray_lfno_primary(Java8Parser.PrimaryNoNewArray_lfno_primaryContext ctx)
    {
        if (ctx.THIS() != null)
        {
            return "this";
        }
        if (ctx.typeName() != null && ctx.DOT() != null && ctx.THIS() != null)
        {
            return visitTypeName(ctx.typeName()) + "->this";
        }
        
        return super.visitPrimaryNoNewArray_lfno_primary(ctx);
    }

    /*@Override
     public Object visitPrimaryNoNewArray_lf_primary(Java8Parser.PrimaryNoNewArray_lf_primaryContext ctx)
     {
     System.out.println("p="+ctx.getText());
     return "pp";
     }*/

    @Override
    public Object visitPrimaryNoNewArray_lfno_arrayAccess(Java8Parser.PrimaryNoNewArray_lfno_arrayAccessContext ctx)
    {
        if (ctx.VOID() != null)
        {
            //TODO
            return "Void.class";
        }
        if (ctx.THIS() != null)
        {
            return "this";
        }
        if (ctx.typeName() != null && ctx.DOT() != null && ctx.THIS() != null)
        {
            return visit(ctx.typeName()) + "->this";
        }
        return super.visitPrimaryNoNewArray_lfno_arrayAccess(ctx);
    }

    @Override
    public Object visitPrimary(Java8Parser.PrimaryContext ctx)
    {
        StringBuilder sb=new StringBuilder();
        if(ctx.primaryNoNewArray_lfno_primary()!=null){
            sb.append(visitPrimaryNoNewArray_lfno_primary(ctx.primaryNoNewArray_lfno_primary()));
        }else if(ctx.arrayCreationExpression()!=null){
            sb.append(visit(ctx.arrayCreationExpression()));
        }
        for(PrimaryNoNewArray_lf_primaryContext p:ctx.primaryNoNewArray_lf_primary()){
            sb.append(visitPrimaryNoNewArray_lf_primary(p));
        }
        return sb.toString();
    }

    

    /*@Override
     public Object visitClassInstanceCreationExpression(Java8Parser.ClassInstanceCreationExpressionContext ctx)
     {
     System.out.println("ins="+ctx.getText());
     return super.visitClassInstanceCreationExpression(ctx);
     }*/

    @Override
    public Object visitClassInstanceCreationExpression_lfno_primary(Java8Parser.ClassInstanceCreationExpression_lfno_primaryContext ctx)
    {
        //System.out.println("nop="+ctx.getText());
        StringBuilder sb=new StringBuilder();

        if (ctx.expressionName() != null)
        {
            sb.append(visit(ctx.expressionName()));
            sb.append("->");
        }

        sb.append("new ");
        List<String> list=new ArrayList<>();
        for (TerminalNode tn:ctx.Identifier())
        {
            list.add(tn.getText());
        }
        sb.append(list.get(0));
        for (int i=1;i < list.size();i++)
        {
            sb.append("->");
            sb.append(list.get(i));
        }
        sb.append("(");
        if (ctx.argumentList() != null)
        {
            sb.append(visit(ctx.argumentList()));
        }
        if (ctx.classBody() != null)
        {
            throw new RuntimeException("body");
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public Object visitClassInstanceCreationExpression_lf_primary(Java8Parser.ClassInstanceCreationExpression_lf_primaryContext ctx)
    {
        StringBuilder sb=new StringBuilder();
        sb.append(".new");
        sb.append(ctx.Identifier().getText());
        sb.append("(");
        if (ctx.argumentList() != null)
        {
            sb.append(visit(ctx.argumentList()));
        }
        sb.append(")");
        if (ctx.classBody() != null)
        {
            throw new RuntimeException("body");
        }
        return sb.toString();
    }

    @Override
    public Object visitConditionalExpression(Java8Parser.ConditionalExpressionContext ctx)
    {
        StringBuilder sb=new StringBuilder();
        String or=(String)visitConditionalOrExpression(ctx.conditionalOrExpression());
        sb.append(or);
        if (ctx.QUESTION() != null)
        {
            sb.append("?");
            sb.append(visitExpression(ctx.expression()));
            sb.append(":");
            sb.append(visitConditionalExpression(ctx.conditionalExpression()));
        }  
        return sb.toString();
    }

    @Override
    public Object visitEqualityExpression(Java8Parser.EqualityExpressionContext ctx)
    {
        StringBuilder sb=new StringBuilder();
        if (ctx.EQUAL() != null)
        {
            sb.append(visit(ctx.equalityExpression()));
            sb.append("==");
        }
        else if (ctx.NOTEQUAL() != null)
        {
            sb.append(visit(ctx.equalityExpression()));
            sb.append("!=");
        }
        sb.append(visit(ctx.relationalExpression()));
        return sb.toString();
    }

    @Override
    public Object visitAdditiveExpression(Java8Parser.AdditiveExpressionContext ctx)
    {
        StringBuilder sb=new StringBuilder();
        if (ctx.ADD() != null)
        {
            sb.append(visitAdditiveExpression(ctx.additiveExpression()));
            sb.append("+");
            //TODO make both sides string if any
        }
        else if (ctx.SUB() != null)
        {
            sb.append(visitAdditiveExpression(ctx.additiveExpression()));
            sb.append("-");
        }
        sb.append(visitMultiplicativeExpression(ctx.multiplicativeExpression()));
        //System.out.println("add="+sb);
        return sb.toString();
    }

    @Override
    public Object visitMultiplicativeExpression(Java8Parser.MultiplicativeExpressionContext ctx)
    {
        StringBuilder sb=new StringBuilder();
        if(ctx.multiplicativeExpression()!=null){
            sb.append(visitMultiplicativeExpression(ctx.multiplicativeExpression()));
        }
        if (ctx.MUL() != null)
        {
            sb.append("*");
        }
        else if (ctx.DIV() != null)
        {
            sb.append("/");
        }
        else if (ctx.MOD() != null)
        {
            sb.append("%");
        }
        sb.append(visitUnaryExpression(ctx.unaryExpression()));
        //System.out.println("add="+sb);
        return sb.toString();
    }

    @Override
    public Object visitUnaryExpression(Java8Parser.UnaryExpressionContext ctx)
    {
        StringBuilder sb=new StringBuilder();
        if(ctx.ADD()!=null){
            sb.append("+");
            sb.append(visitUnaryExpression(ctx.unaryExpression()));
        }
        else if(ctx.SUB()!=null){
            sb.append("-");
            sb.append(visitUnaryExpression(ctx.unaryExpression()));
        }else{
            sb.append(super.visitUnaryExpression(ctx));
        }
        return sb.toString();
    }

    @Override
    public Object visitUnaryExpressionNotPlusMinus(Java8Parser.UnaryExpressionNotPlusMinusContext ctx)
    {
        StringBuilder sb=new StringBuilder();
        if(ctx.TILDE()!=null){
            sb.append("~");
            sb.append(visitUnaryExpression(ctx.unaryExpression()));
        }else if(ctx.BANG()!=null){
            sb.append("!");
            sb.append(visitUnaryExpression(ctx.unaryExpression()));
        }else{
            sb.append(super.visitUnaryExpressionNotPlusMinus(ctx));
        }
        return sb.toString();
    }

    /*@Override
    public Object visitPostfixExpression(Java8Parser.PostfixExpressionContext ctx)
    {
        System.out.println("post="+ctx.getText()+","+ctx.children.get(0).getClass());
        return "post";
    }*/

    @Override
    public Object visitLiteral(Java8Parser.LiteralContext ctx)
    {
        if (ctx.NullLiteral() != null)
        {
            return "nullptr";
        }
        else if (ctx.IntegerLiteral() != null)
        {
            return ctx.IntegerLiteral().getText();
        }
        else if (ctx.StringLiteral() != null)
        {
            return "new String(" + ctx.StringLiteral().getText() + ")";
        }
        return super.visitLiteral(ctx);
    }

    @Override
    public Object visitIfThenStatement(Java8Parser.IfThenStatementContext ctx)
    {
        StringBuilder sb=new StringBuilder();
        sb.append("if(");
        sb.append(visitExpression(ctx.expression()));
        sb.append(")");
        sb.append(visitStatement(ctx.statement()));
        //sb.append("\n}\n");
        //body.append(sb.toString());
        return sb.toString();
    }

    @Override
    public Object visitBlock(Java8Parser.BlockContext ctx)
    {
        return "{\n" + visitBlockStatements(ctx.blockStatements()) + "}\n";
    }

    @Override
    public Object visitBlockStatements(Java8Parser.BlockStatementsContext ctx)
    {
        StringBuilder sb=new StringBuilder();

        for (BlockStatementContext bsc:ctx.blockStatement())
        {
            sb.append(visitBlockStatement(bsc));
            sb.append("\n");
        }
        return sb.toString();
    }
    @Override
    public Object visitMethodBody(Java8Parser.MethodBodyContext ctx)
    {
        if (ctx.block() != null)
        {
            body.append((String)visitBlock(ctx.block()));
        }
        return null;
    }

    @Override
    public Object visitConstructorBody(Java8Parser.ConstructorBodyContext ctx)
    {
        body.appendln("{");
        if (ctx.explicitConstructorInvocation() != null)
        {
            //TODO 
        }
        if (ctx.blockStatements() != null)
        {
            body.append(((String)visitBlockStatements(ctx.blockStatements())));
        }
        body.appendln("}");
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
