package com.mesut.j2cpp.visitor;

import com.github.javaparser.ast.visitor.*;
import com.mesut.j2cpp.*;
import com.github.javaparser.ast.expr.*;
import com.mesut.j2cpp.ast.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.*;
import java.io.*;
import java.util.*;

public class MethodVisitor extends VoidVisitorAdapter<Nodew>
{
    //public boolean firstBlock=false;
    
    public void visit(BlockStmt n,Nodew w){
        w.firstBlock=true;
        w.appendln("{");
        w.up();
        for(Statement st:n.getStatements()){
            st.accept(this,w);
            w.println();
        }
        w.down();
        w.append("}");
        //body.append(w);
    }
    
    public void visit(ExpressionStmt n,Nodew w){
        n.getExpression().accept(this,w);
        w.append(";");
        //body.append(w);
    }
    
    public void visit(IfStmt n,Nodew w){
        w.append("if(");
        n.getCondition().accept(this,w);
        w.append(")");
        n.getThenStmt().accept(this,w);
        if(n.hasElseBranch()){
            w.append("else");
            if(n.hasCascadingIfStmt()){
                w.append(" ");
            }
            n.getElseStmt().get().accept(this,w);
        }
    }
    
    public void visit(WhileStmt n,Nodew w){
        w.append("while(");
        n.getCondition().accept(this,w);
        w.append(")");
        n.getBody().accept(this,w); 
    }
    
    public void visit(ForStmt n,Nodew w){
        w.append("for(");
        int i=0,l=n.getInitialization().size()-1;
        for(Expression e:n.getInitialization()){
            e.accept(this,w);
            if(i<l){
                w.append(",");
                i++;
            }
        }
        w.append(";");
        if(n.getCompare().isPresent()){
            n.getCompare().get().accept(this,w);
        }
        w.append(";");
        int j=0,l2=n.getUpdate().size()-1;
        for(Expression e:n.getUpdate()){
            e.accept(this,w);
            if(j<l2){
                w.append(",");
                j++;
            }
        }
        w.append(")");
        n.getBody().accept(this,w);
    }
    
    public void visit(ForEachStmt n,Nodew w){
        w.append("for(");
        n.getVariable().accept(this,w);
        w.append(":");
        n.getIterable().accept(this,w);
        w.append(")");
        n.getBody().accept(this,w);
    }
    
    public void visit(ReturnStmt n,Nodew w){
        w.append("return ");
        n.getExpression().get().accept(this,w);
        w.append(";");
    }
    
    public void visit(TryStmt n,Nodew w){
        w.append("try");
        int len=n.getResources().size();
        if(len>0){
            w.append("(");
            for(int i=0;i<len;i++){
                n.getResources().get(i).accept(this,w);
                if(i<len-1){
                    w.append(",");
                }
            }
            w.append(")");
        }
        
        
        n.getTryBlock().accept(this,w);
        for(CatchClause cc:n.getCatchClauses()){
            w.append("catch(");
            Parameter p=cc.getParameter();
            w.append(p.getTypeAsString());
            w.append(" ");
            w.append(p.getNameAsString());
            w.append(")");
            cc.getBody().accept(this,w);
        }
        if(n.getFinallyBlock().isPresent()){
            //TODO
            //if try has return stmt,finally is executed before return
            n.getFinallyBlock().get().accept(this,w);
        }
    }
    
    public void visit(ThrowStmt n,Nodew w){
        w.append("throw ");
        n.getExpression().accept(this,w);
    }
    
    public void visit(MethodCallExpr n,Nodew w){
        if(n.getScope().isPresent()){
            n.getScope().get().accept(this,w);
            w.append("->");
        }
        w.append(n.getNameAsString());
        args(n.getArguments(),w);
        /*w.append("(");
        int i=0,l=n.getArguments().size();
        for(Expression exp:n.getArguments()){
            exp.accept(this,w);
            if(i<l-1){
                w.append(",");
            }
        }
        w.append(")");*/
    }
    
    public void visit(AssignExpr n,Nodew w){
        n.getTarget().accept(this,w);
        w.append(n.getOperator().asString());
        n.getValue().accept(this,w);
    }
    
    public void visit(FieldAccessExpr n,Nodew w){
        n.getScope().accept(this,w);
        w.append("->");
        w.append(n.getNameAsString());
    }
    
    public void visit(ThisExpr n,Nodew w){
        w.append("this");
    }
    
    public void visit(NameExpr n,Nodew w){
        w.append(n.getNameAsString());
    }
    
    public void visit(ObjectCreationExpr n,Nodew w){
        if (n.getScope().isPresent()) {
            n.getScope().get().accept(this, w);
            w.append("->");
        }
        
        w.append("new ");
        //typearg
        w.append(n.getTypeAsString());
        args(n.getArguments(),w);
        if(n.getAnonymousClassBody().isPresent()){
            
        }
    }
    
    public void visit(NodeList n,Nodew w){
        w.append("(");
        for(Iterator<Expression> i=n.iterator();i.hasNext();){
            i.next().accept(this,w);
            if(i.hasNext()){
                w.append(",");
            }
        }
        w.append(")");
    }
    
    public void args(NodeList<Expression> l,Nodew w){
        w.append("(");
        for(int i=0;i<l.size();i++){
            l.get(i).accept(this,w);
            if(i<l.size()-1){
                w.append(",");
            }
        }
        w.append(")");
    }
    
    public void visit(VariableDeclarationExpr n,Nodew w){
        boolean first=true;
        for(VariableDeclarator vd:n.getVariables()){
            if(first){
                first=false;
                TypeName t=new TypeName(vd.getTypeAsString());
                w.append(t.full());
                if(t.isPointer()){
                    w.append("*");
                }
                w.append(" ");
            }else{
                w.append(",");
            }
            
            w.append(vd.getNameAsString());
            if(vd.getInitializer().isPresent()){
                w.append("=");
                vd.getInitializer().get().accept(this,w);
            }
            
        }
    }
    
    public void visit(ConditionalExpr n,Nodew w){
        n.getCondition().accept(this,w);
        w.append("?");
        n.getThenExpr().accept(this,w);
        w.append(":");
        n.getElseExpr().accept(this,w);
    }
    
    public void visit(BinaryExpr n,Nodew w){
        n.getLeft().accept(this,w);
        w.append(n.getOperator().asString());
        n.getRight().accept(this,w);
    }
    
    public void visit(NullLiteralExpr n,Nodew w){
        w.append("nullptr");
    }
    public void visit(IntegerLiteralExpr n,Nodew w){
        w.append(n.getValue());
    }
    public void visit(StringLiteralExpr n,Nodew w){
        w.append("new String(\"");
        w.append(n.getValue());
        w.append("\")");
    }
    
    public void visit(ArrayCreationExpr n,Nodew w){
        w.append("new ");
        w.append(n.getElementType().asString());
        for(ArrayCreationLevel cl:n.getLevels()){
            w.append("[");
            if(cl.getDimension().isPresent()){
                cl.getDimension().get().accept(this,w);
            }
            w.append("]");
        }
        if(n.getInitializer().isPresent()){
            //TODO
        }
    }
    
}
