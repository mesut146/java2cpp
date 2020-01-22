package com.mesut.j2cpp.visitor;

import com.github.javaparser.ast.ArrayCreationLevel;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.*;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;
import com.mesut.j2cpp.Converter;
import com.mesut.j2cpp.Helper;
import com.mesut.j2cpp.Nodew;
import com.mesut.j2cpp.ast.CHeader;
import com.mesut.j2cpp.ast.CMethod;
import com.mesut.j2cpp.ast.CType;
import com.mesut.j2cpp.ast.Call;

import java.util.Iterator;

public class MethodVisitor extends GenericVisitorAdapter<Object, Nodew> {
    public CMethod method;
    public CHeader header;
    public Converter converter;
    boolean hasReturn = false, hasThrow = false, hasBreak = false;

    public MethodVisitor(Converter converter, CHeader header) {
        this.header = header;
        this.converter = converter;
    }

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
        block(w, n.getThenStmt());
        if (n.getElseStmt().isPresent()) {
            w.append("else");
            block(w, n.getElseStmt().get());
        }
        return null;
    }

    void block(Nodew w, Statement statement) {
        if (statement.isBlockStmt()) {
            statement.accept(this, w);
        } else {
            w.println();
            w.up();
            statement.accept(this, w);
            w.down();
        }
    }

    public Object visit(WhileStmt n, Nodew w) {
        w.append("while(");
        n.getCondition().accept(this, w);
        w.append(") ");
        block(w, n.getBody());
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
        block(w, n.getBody());
        return null;
    }

    public Object visit(ForEachStmt n, Nodew w) {
        w.append("for(");
        n.getVariable().accept(this, w);
        w.append(":");
        n.getIterable().accept(this, w);
        w.append(")");
        block(w, n.getBody());
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
            header.addRuntime();
            String retStr = "if(0){return";
            CType type = method.type;
            if (!method.isCons && !method.type.isVoid()) {
                retStr += "" + type.toString();
            } else {
                type = new CType("void");
            }
            retStr += ";}";
            Nodew fin = new Nodew();
            //tryRet=hasReturn;hasReturn=false;
            n.getFinallyBlock().get().accept(this, fin);
            //finRet=hasReturn;hasReturn=false;
            w.append("bool tryReturned=true,finReturned=true;");
            w.line(type.toString()).append("* res_tryBlock=with_finally<" + type.toString() + ">([&](){");
            w.up();
            w.line("try");
            Nodew tn = new Nodew();
            n.getTryBlock().accept(this, tn);
            w.appendi(tn);
            if (n.getCatchClauses().size() == 0) {
                w.line("catch(int x){}");
            }
            makeCatch(n.getCatchClauses(), w);
            w.line(retStr);
            w.line("tryReturned=false;");
            w.down();
            w.lineln("},[&](){");
            w.up();
            w.appendi(fin);
            w.line(retStr);
            w.line("finReturned=false;");
            w.down();
            w.line("});");
            if (!method.isCons && !method.type.isVoid()) {
                w.line("if(tryReturned||finReturned){");
                w.up();
                w.line("return res_tryBlock;");
                w.down();
                w.line("}");
            }
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
            if (n.getCatchClauses().size() == 0) {
                w.append("catch(int x){}");
            }
            makeCatch(n.getCatchClauses(), w);
        }
        return null;
    }

    void makeCatch(NodeList<CatchClause> list, Nodew w) {
        for (CatchClause cc : list) {
            Parameter p = cc.getParameter();
            if (p.getType().isUnionType()) {
                for (Type t : p.getType().asUnionType().getElements()) {
                    w.append("catch(");
                    w.append((CType) t.accept(this, new Nodew()));
                    w.append(" ");
                    w.append(p.getNameAsString());
                    w.append(")");
                    cc.getBody().accept(this, w);
                }
            } else {
                w.append("catch(");
                w.append((CType) p.getType().accept(this, new Nodew()));
                w.append(" ");
                w.append(p.getNameAsString());
                w.append(")");
                cc.getBody().accept(this, w);
            }

        }
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

    public Object visit(InstanceOfExpr n, Nodew w) {
        header.addRuntime();
        w.append("instance_of<");
        CType type = (CType) n.getType().accept(this, null);
        w.append(type.toString());
        w.append(">(");
        n.getExpression().accept(this, w);
        w.append(")");
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

            if (scope.isNameExpr()) {//field or class
                if (converter.getResolver().isClass(scope.asNameExpr().getNameAsString(), header)) {
                    w.append("::");
                } else {
                    w.append("->");
                }
            } else {
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
        Expression scope = n.getScope();
        scope.accept(this, w);
        //TODO:if scope is not object,ns
        if (scope.isNameExpr()) {//field/var or class
            if (converter.getResolver().isClass(scope.asNameExpr().getNameAsString(), header)) {
                w.append("::");
            } else {
                w.append("->");
            }
        } else {
            w.append("->");
        }
        w.append(n.getNameAsString());
        return null;
    }

    @Override
    public Object visit(ArrayAccessExpr n, Nodew w) {
        w.append("(*");
        n.getName().accept(this, w);
        w.append(")[");
        n.getIndex().accept(this, w);
        w.append("]");
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

    public Object visit(ObjectCreationExpr n, Nodew w) {
        if (n.getScope().isPresent()) {
            n.getScope().get().accept(this, w);
            w.append("->");
        }

        w.append("new ");
        //typearg
        CType type = (CType) n.getType().accept(this, null);
        w.append(type.toString());
        args(n.getArguments(), w);
        if (n.getAnonymousClassBody().isPresent()) {
            //TODO
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
                CType t = (CType) vd.getType().accept(this, null);
                w.append(t.toString());
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

    @Override
    public Object visit(CharLiteralExpr n, Nodew w) {

        String str = n.getValue();
        if (str.startsWith("\\u")) {
            w.append("u");
        }
        w.append("'");
        w.append(str);
        w.append("'");
        return null;
    }

    @Override
    public Object visit(CastExpr n, Nodew w) {
        w.append("(");
        CType type = (CType) n.getType().accept(this, w);
        w.append(type);
        if (type.isPointer()) {
            w.append("*");
        }
        w.append(")");
        n.getExpression().accept(this, w);
        return null;
    }

    @Override
    public Object visit(EnclosedExpr n, Nodew w) {
        w.append("(");
        n.getInner().accept(this, w);
        w.append(")");
        return null;
    }

    @Override
    public Object visit(LongLiteralExpr n, Nodew w) {
        String str = n.getValue();
        if (str.endsWith("L")) {
            str = str.substring(0, str.length() - 1);
        }
        w.append(str);
        return null;
    }

    public Object visit(StringLiteralExpr n, Nodew w) {
        w.append("new String(\"");
        w.append(n.getValue());
        w.append("\")");
        return null;
    }

    @Override
    public Object visit(BooleanLiteralExpr n, Nodew w) {
        if (n.getValue()) {
            w.append("true");
        } else {
            w.append("false");
        }
        return null;
    }

    //int[]{}
    public Object visit(ArrayCreationExpr n, Nodew w) {
        if (n.getInitializer().isPresent()) {
            n.getInitializer().get().accept(this, w);
        } else {
            //only dimensions
            w.append("new ");
            CType typeName = new CType(n.getElementType().asString());
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

    public Object visit(PrimitiveType n, Nodew w) {
        CType typeName = new CType(Helper.toCType(n.asString()));
        //w.append(typeName.toString());
        return typeName;
    }

    public Object visit(ArrayType n, Nodew w) {
        CType typeName = (CType) n.getElementType().accept(this, w);
        typeName.arrayLevel = n.getArrayLevel();
        return typeName;
    }

    public Object visit(VoidType n, Nodew w) {
        CType typeName = new CType("void");
        //w.append(typeName.toString());
        return typeName;
    }

    public Object visit(ClassOrInterfaceType n, Nodew w) {
        CType typeName = new CType(n.getNameAsString());
        if (n.getTypeArguments().isPresent()) {
            for (Iterator<Type> iterator = n.getTypeArguments().get().iterator(); iterator.hasNext(); ) {
                typeName.typeNames.add((CType) iterator.next().accept(this, w));
            }
        }
        if (n.getScope().isPresent()) {
            CType scope = (CType) n.getScope().get().accept(this, new Nodew());
            typeName.type = scope.type + "::" + typeName.type;
        }
        //w.append(typeName.toString());
        return typeName;
    }

    public Object visit(UnionType n, Nodew w) {
        CType type = (CType) n.getElements().get(0).accept(this, new Nodew());
        System.out.println("union type detected and chosen the first");
        return type;
    }

    public Object visit(NameExpr n, Nodew w) {
        w.append(n.getNameAsString());
        return null;
    }

    public Object visit(SimpleName n, Nodew w) {
        CType typeName = new CType(n.getIdentifier());
        //w.append(typeName.toString());
        return typeName;
    }

}
