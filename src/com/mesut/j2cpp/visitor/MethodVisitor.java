package com.mesut.j2cpp.visitor;

import com.github.javaparser.ast.type.*;
import com.github.javaparser.ast.visitor.*;
import com.mesut.j2cpp.*;
import com.github.javaparser.ast.expr.*;
import com.mesut.j2cpp.ast.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.*;
import com.mesut.j2cpp.ast.Node;

import java.util.*;

public class MethodVisitor extends GenericVisitorAdapter<Object, Nodew> {
    public CMethod method;
    public CHeader header;
    public SymbolTable table;
    boolean hasReturn = false, hasThrow = false, hasBreak = false;

    public Object visit(BlockStmt n, Nodew w) {
        w.firstBlock = true;
        w.appendln("{");
        w.up();
        for (Statement st : n.getStatements()) {
            st.accept(this, w);
            w.println();
        }
        w.down();
        w.append("}");
        return null;
    }

    public Object visit(ExpressionStmt n, Nodew w) {
        n.getExpression().accept(this, w);
        w.append(";");
        return null;
    }

    public Object visit(IfStmt n, Nodew w) {
        w.append("if(");
        n.getCondition().accept(this, w);
        w.append(")");
        n.getThenStmt().accept(this, w);
        if (n.getElseStmt().isPresent()) {
            w.append("else");
            if (n.hasCascadingIfStmt()) {
                w.append(" ");
            }
            n.getElseStmt().get().accept(this, w);
        }
        return null;
    }

    public Object visit(WhileStmt n, Nodew w) {
        w.append("while(");
        n.getCondition().accept(this, w);
        w.append(")");
        n.getBody().accept(this, w);
        return null;
    }

    public Object visit(ForStmt n, Nodew w) {
        w.append("for(");
        int i = 0, l = n.getInitialization().size() - 1;
        for (Expression e : n.getInitialization()) {
            e.accept(this, w);
            if (i < l) {
                w.append(",");
                i++;
            }
        }
        w.append(";");
        if (n.getCompare().isPresent()) {
            n.getCompare().get().accept(this, w);
        }
        w.append(";");
        int j = 0, l2 = n.getUpdate().size() - 1;
        for (Expression e : n.getUpdate()) {
            e.accept(this, w);
            if (j < l2) {
                w.append(",");
                j++;
            }
        }
        w.append(")");
        n.getBody().accept(this, w);
        return null;
    }

    public Object visit(ForEachStmt n, Nodew w) {
        w.append("for(");
        n.getVariable().accept(this, w);
        w.append(":");
        n.getIterable().accept(this, w);
        w.append(")");
        n.getBody().accept(this, w);
        return null;
    }

    public Object visit(ReturnStmt n, Nodew w) {
        hasReturn = true;
        w.append("return");
        if (n.getExpression().isPresent()) {
            w.append(" ");
            n.getExpression().get().accept(this, w);
        }
        w.append(";");
        return null;
    }

    public Object visit(TryStmt n, Nodew w) {
        boolean tryRet, tryTh, finRet, finTh;
        if (n.getFinallyBlock().isPresent()) {
            //String to=method.type.type;
            Nodew fin = new Nodew();
            Nodew tn = new Nodew();
            n.getTryBlock().accept(this, tn);
            //tryRet=hasReturn;hasReturn=false;
            n.getFinallyBlock().get().accept(this, fin);
            //finRet=hasReturn;hasReturn=false;
            w.line("bool tryReturned=true,finReturned=true;");
            w.line("void* res_tryBlock=with_finally([&](){");
            w.up();
            w.line("try");
            w.append(tn);
            if (n.getCatchClauses().size() > 0) {
                for (CatchClause cc : n.getCatchClauses()) {
                    w.append("catch(");
                    Parameter p = cc.getParameter();
                    w.append(p.getTypeAsString());
                    w.append(" ");
                    w.append(p.getNameAsString());
                    w.append(")");
                    cc.getBody().accept(this, w);
                }
            } else {
                w.line("catch(int x){}");
            }
            w.line("if(0){return nullptr;}");
            w.line("tryReturned=false");
            w.down();
            w.line("},[&](){");
            w.up();
            w.append(fin);
            w.line("if(0){return nullptr;}");
            w.line("finReturned=false");
            w.down();
            w.line("});");

            w.line("if(tryReturned||finReturned){");
            w.up();
            w.line("return res_tryBlock;");
            w.line("}");
        } else {
            w.append("try");
            int len = n.getResources().size();
            if (len > 0) {
                w.append("(");
                for (int i = 0; i < len; i++) {
                    n.getResources().get(i).accept(this, w);
                    if (i < len - 1) {
                        w.append(",");
                    }
                }
                w.append(")");
            }
            n.getTryBlock().accept(this, w);
            for (CatchClause cc : n.getCatchClauses()) {
                w.append("catch(");
                Parameter p = cc.getParameter();
                w.append(p.getTypeAsString());
                w.append(" ");
                w.append(p.getNameAsString());
                w.append(")");
                cc.getBody().accept(this, w);
            }
        }

        return null;
    }

    void makeLambda(BlockStmt n, Nodew w) {
        w.append("auto finally=[&]()");
        n.accept(this, w);
        w.append(";");
    }

    public Object visit(ThrowStmt n, Nodew w) {
        hasThrow = true;
        w.append("throw ");
        n.getExpression().accept(this, w);
        return null;
    }

    public Object visit(ExplicitConstructorInvocationStmt n, Nodew w) {
        Nodew p = new Nodew();
        Call c = new Call();
        c.isThis = n.isThis();
        if (n.isThis()) {
            p.line(method.getName());
        } else {
            if (n.getExpression().isPresent()) {
                return null;
            } else {
                p.line(method.getParent().base.get(0).type);
            }

        }
        args(n.getArguments(), p);
        c.str = p.toString();
        method.call = c;
        return null;
    }

    public Object visit(MethodCallExpr n, Nodew w) {
        if (n.getScope().isPresent()) {
            Expression scope = n.getScope().get();
            scope.accept(this, w);
            Nodew scp = new Nodew();
            scope.accept(this, scp);

            if (scope.isNameExpr()){//field or class
                if (Resolver.isClass(scope.asNameExpr().getNameAsString())){
                    w.append("::");
                }else{
                    w.append("->");
                }
            }else {
                w.append("->");
            }
            /*if (scope.isFieldAccessExpr()||scope.isMethodCallExpr()){
                w.append("->");
            }
            */
            //TODO: if static access,make ns
        }
        w.append(n.getNameAsString());
        args(n.getArguments(), w);
        return null;
    }

    public Object visit(FieldAccessExpr n, Nodew w) {
        n.getScope().accept(this, w);
        //TODO:if scope is not object,ns
        Nodew scp = new Nodew();
        n.getScope().accept(this, scp);
        if (method != null) {
            if (method.getParent().hasFieldAny(scp.toString()) || scp.cache.equals("this")) {
                w.append("->");
            } else {
                w.append("::");
            }
        } else {
            //ns
            w.append("->");
        }
        //w.append("->");
        w.append(n.getNameAsString());
        return null;
    }

    public Object visit(AssignExpr n, Nodew w) {
        n.getTarget().accept(this, w);
        w.append(n.getOperator().asString());
        n.getValue().accept(this, w);
        return null;
    }

    public Object visit(ThisExpr n, Nodew w) {
        w.append("this");
        return null;
    }

    public Object visit(NameExpr n, Nodew w) {
        w.append(n.getNameAsString());
        return null;
    }

    public Object visit(ObjectCreationExpr n, Nodew w) {
        if (n.getScope().isPresent()) {
            n.getScope().get().accept(this, w);
            w.append("->");
        }

        w.append("new ");
        //typearg
        w.append(n.getTypeAsString());
        args(n.getArguments(), w);
        if (n.getAnonymousClassBody().isPresent()) {

        }
        return null;
    }

    public Object visit(NodeList<Expression> n, Nodew w) {
        w.append("(");
        for (Iterator<Expression> i = n.iterator(); i.hasNext(); ) {
            i.next().accept(this, w);
            if (i.hasNext()) {
                w.append(",");
            }
        }
        w.append(")");
        return null;
    }

    public Object args(NodeList<Expression> l, Nodew w) {
        w.append("(");
        for (int i = 0; i < l.size(); i++) {
            l.get(i).accept(this, w);
            if (i < l.size() - 1) {
                w.append(",");
            }
        }
        w.append(")");
        return null;
    }

    public Object visit(VariableDeclarationExpr n, Nodew w) {
        boolean first = true;
        for (VariableDeclarator vd : n.getVariables()) {
            if (first) {
                first = false;
                TypeName t = (TypeName) vd.getType().accept(this, null);
                w.append(t.full());
                if (t.isPointer()) {
                    w.append("*");
                }
                w.append(" ");
            } else {
                w.append(",");
            }

            w.append(vd.getNameAsString());
            if (vd.getInitializer().isPresent()) {
                w.append("=");
                vd.getInitializer().get().accept(this, w);
            }

        }
        return null;
    }

    public Object visit(ConditionalExpr n, Nodew w) {
        n.getCondition().accept(this, w);
        w.append("?");
        n.getThenExpr().accept(this, w);
        w.append(":");
        n.getElseExpr().accept(this, w);
        return null;
    }

    public Object visit(BinaryExpr n, Nodew w) {
        n.getLeft().accept(this, w);
        w.append(n.getOperator().asString());
        n.getRight().accept(this, w);
        return null;
    }

    public Object visit(UnaryExpr n, Nodew w) {
        if (n.isPostfix()) {
            n.getExpression().accept(this, w);
            w.append(n.getOperator().asString());
        } else {
            w.append(n.getOperator().asString());
            n.getExpression().accept(this, w);
        }
        return null;
    }

    public Object visit(NullLiteralExpr n, Nodew w) {
        w.append("nullptr");
        return null;
    }

    public Object visit(IntegerLiteralExpr n, Nodew w) {
        w.append(n.getValue());
        return null;
    }

    public Object visit(StringLiteralExpr n, Nodew w) {
        w.append("new String(\"");
        w.append(n.getValue());
        w.append("\")");
        return null;
    }

    //int[]{}
    public Object visit(ArrayCreationExpr n, Nodew w) {
        if (n.getInitializer().isPresent()) {
            n.getInitializer().get().accept(this, w);
        } else {
            //only dimensions
            w.append("new ");
            TypeName typeName = new TypeName(n.getElementType().asString());
            typeName.arrayLevel = n.getLevels().size();
            w.append(typeName.toString());
            w.append("(");
            int i = 0;
            w.append("new int[]{");
            for (ArrayCreationLevel cl : n.getLevels()) {
                if (cl.getDimension().isPresent()) {
                    cl.getDimension().get().accept(this, w);
                } else {
                    w.append("0");
                }
                if (i < typeName.arrayLevel - 1) {
                    w.append(",");
                    i++;
                }
            }
            w.append("},");
            w.append(String.valueOf(typeName.arrayLevel));
            w.append(")");
        }
        return null;
    }

    //{{1,2,3},{4,5,6}}
    public Object visit(ArrayInitializerExpr n, Nodew w) {
        w.append("{");
        for (Iterator<Expression> iterator = n.getValues().iterator(); iterator.hasNext(); ) {
            iterator.next().accept(this, w);
            if (iterator.hasNext()) {
                w.append(",");
            }
        }
        w.append("}");
        return null;
    }

    /*public Object visit(Type n, Nodew w){
        return n.accept(this,w);
    }*/

    public Object visit(ArrayType n, Nodew w) {
        TypeName typeName = new TypeName(n.getComponentType().asString());
        //typeName.arrayLevel=1;
        //System.out.println("arr type="+typeName);
        return typeName;
    }

    public Object visit(VoidType n, Nodew w) {
        TypeName typeName = new TypeName("void");
        //w.append(typeName.toString());
        return typeName;
    }

    public Object visit(ClassOrInterfaceType n, Nodew w) {
        TypeName typeName = new TypeName(n.getNameAsString());
        //w.append(typeName.toString());
        return typeName;
    }

    public Object visit(SimpleName n, Nodew w) {
        TypeName typeName = new TypeName(n.getIdentifier());
        //w.append(typeName.toString());
        return typeName;
    }

    public Object visit(PrimitiveType n, Nodew w) {
        TypeName typeName = new TypeName(n.asString());
        //w.append(typeName.toString());
        return typeName;
    }
}
