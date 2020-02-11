package com.mesut.j2cpp.visitor;

import com.github.javaparser.ast.ArrayCreationLevel;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;
import com.mesut.j2cpp.Converter;
import com.mesut.j2cpp.Nodew;
import com.mesut.j2cpp.ast.CHeader;
import com.mesut.j2cpp.ast.CMethod;
import com.mesut.j2cpp.ast.CType;

import java.util.Iterator;
import java.util.Optional;

public class ExprVisitor extends GenericVisitorAdapter<Object, Nodew> {

    public CMethod method;
    public CHeader header;
    public Converter converter;
    TypeVisitor typeVisitor;
    boolean hasReturn = false, hasThrow = false, hasBreak = false;

    public ExprVisitor(Converter converter, CHeader header, TypeVisitor typeVisitor) {
        this.converter = converter;
        this.header = header;
        this.typeVisitor = typeVisitor;
    }

    public Object visit(SimpleName n, Nodew w) {
        return new CType(n.getIdentifier());
    }

    public void args(NodeList<Expression> expressions, Nodew w) {
        w.append("(");
        for (int i = 0; i < expressions.size(); i++) {
            expressions.get(i).accept(this, w);
            if (i < expressions.size() - 1) {
                w.append(",");
            }
        }
        w.append(")");
    }

    public Object visit(InstanceOfExpr n, Nodew w) {
        header.addRuntime();
        w.append("instance_of<");
        CType type = (CType) n.getType().accept(typeVisitor, null);
        w.append(type.toString());
        w.append(">(");
        n.getExpression().accept(this, w);
        w.append(")");
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
                //another method call or field access
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
                //another field access or method call
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
        CType type = (CType) n.getType().accept(typeVisitor, null);
        w.append(type.toString());
        args(n.getArguments(), w);
        if (n.getAnonymousClassBody().isPresent()) {
            //TODO
        }
        return null;
    }

    public Object visit(VariableDeclarationExpr n, Nodew w) {
        boolean first = true;
        for (VariableDeclarator vd : n.getVariables()) {
            if (first) {
                first = false;
                CType t = (CType) vd.getType().accept(typeVisitor, null);
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
                w.append(" = ");
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
        w.append(" ");
        w.append(n.getOperator().asString());
        w.append(" ");
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
        CType type = (CType) n.getType().accept(typeVisitor, w);
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
        w.append("new java::lang::String(\"");
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
            for (Iterator<ArrayCreationLevel> iterator = n.getLevels().iterator(); iterator.hasNext(); ) {
                Optional<Expression> dimension = iterator.next().getDimension();
                if (dimension.isPresent()) {
                    dimension.get().accept(this, w);
                } else {
                    w.append("0");//default array size
                }
                if (iterator.hasNext()) {
                    w.append(",");
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

    public Object visit(NameExpr n, Nodew w) {
        w.append(n.getNameAsString());
        return null;
    }
}
