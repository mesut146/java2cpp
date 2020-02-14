package com.mesut.j2cpp.visitor;

import com.github.javaparser.ast.ArrayCreationLevel;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.mesut.j2cpp.Converter;
import com.mesut.j2cpp.Writer;
import com.mesut.j2cpp.ast.CClass;
import com.mesut.j2cpp.ast.CHeader;
import com.mesut.j2cpp.ast.CMethod;
import com.mesut.j2cpp.ast.CType;

import java.util.Iterator;
import java.util.Optional;

public class ExprVisitor extends GenericVisitorAdapter<Object, Writer> {

    public CMethod method;
    public CClass clazz;
    public CHeader header;
    public Converter converter;
    TypeVisitor typeVisitor;
    boolean hasReturn = false, hasThrow = false, hasBreak = false;

    public ExprVisitor(Converter converter, CHeader header, TypeVisitor typeVisitor) {
        this.converter = converter;
        this.header = header;
        this.typeVisitor = typeVisitor;
    }

    public void setMethod(CMethod method) {
        this.method = method;
    }

    public Object visit(SimpleName n, Writer w) {
        return new CType(n.getIdentifier());
    }

    public void args(NodeList<Expression> expressions, Writer w) {
        w.append("(");
        for (int i = 0; i < expressions.size(); i++) {
            expressions.get(i).accept(this, w);
            if (i < expressions.size() - 1) {
                w.append(",");
            }
        }
        w.append(")");
    }

    public Object visit(InstanceOfExpr n, Writer w) {
        header.addRuntime();
        w.append("instance_of<");
        CType type = typeVisitor.visitType(n.getType(), method);
        w.append(type);
        w.append(">(");
        n.getExpression().accept(this, w);
        w.append(")");
        return null;
    }

    public Object visit(MethodCallExpr n, Writer w) {
        if (n.getScope().isPresent()) {
            Expression scope = n.getScope().get();
            scope.accept(this, w);

            if (scope.isNameExpr()) {//field or class
                String scopeName = scope.asNameExpr().getNameAsString();
                if (scopeName.equals("this")) {
                    w.append("->");
                    w.append(n.getNameAsString());
                    args(n.getArguments(), w);
                    return null;
                }
                ResolvedMethodDeclaration rt = converter.symbolResolver.resolveDeclaration(n, ResolvedMethodDeclaration.class);

                if (rt.isStatic()) {
                    w.append("::");
                } else {
                    w.append("->");
                }
            } else {
                //another method call or field access
                w.append("->");
            }
        }
        w.append(n.getNameAsString());
        args(n.getArguments(), w);
        return null;
    }

    public Object visit(FieldAccessExpr n, Writer w) {
        Expression scope = n.getScope();
        scope.accept(this, w);

        if (scope.isNameExpr()) {//field/var or class
            String scopeName = scope.asNameExpr().getNameAsString();
            if (scopeName.equals("this")) {
                w.append("->");
                w.append(n.getNameAsString());
                return null;
            }
            ResolvedFieldDeclaration rt = converter.symbolResolver.resolveDeclaration(n, ResolvedFieldDeclaration.class);
            if (rt.isStatic()) {
                w.append("::");
            } else {
                w.append("->");
            }
            //System.out.println("resolved="+rt+" "+rt.isStatic());

        } else {//another expr
            w.append("->");
        }
        w.append(n.getNameAsString());
        return null;
    }

    //array[index] -> (*array)[index]
    @Override
    public Object visit(ArrayAccessExpr n, Writer w) {
        w.append("(*");
        n.getName().accept(this, w);
        w.append(")[");
        n.getIndex().accept(this, w);
        w.append("]");
        return null;
    }

    public Object visit(AssignExpr n, Writer w) {
        n.getTarget().accept(this, w);
        w.append(" ");
        w.append(n.getOperator().asString());
        w.append(" ");
        n.getValue().accept(this, w);
        return null;
    }

    public Object visit(ThisExpr n, Writer w) {
        w.append("this");
        return null;
    }

    //new Base.Inner(args...){body}
    public Object visit(ObjectCreationExpr n, Writer w) {
        if (n.getScope().isPresent()) {
            n.getScope().get().accept(this, w);
            w.append("->");
        }
        w.append("new ");
        //typearg
        CType type;
        if (method == null) {
            type = typeVisitor.visitType(n.getType(), clazz);
        } else {
            type = typeVisitor.visitType(n.getType(), method);
        }

        type.isPointer = false;//new keyword already makes it pointer
        w.append(type.toString());
        args(n.getArguments(), w);
        if (n.getAnonymousClassBody().isPresent()) {
            //TODO
        }
        return null;
    }

    //Type name=value
    public Object visit(VariableDeclarationExpr n, Writer w) {
        boolean first = true;
        for (VariableDeclarator vd : n.getVariables()) {
            if (first) {
                first = false;
                CType type = typeVisitor.visitType(vd.getType(), method);
                type.isTemplate = false;
                w.append(type);
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

    public Object visit(ConditionalExpr n, Writer w) {
        n.getCondition().accept(this, w);
        w.append("?");
        n.getThenExpr().accept(this, w);
        w.append(":");
        n.getElseExpr().accept(this, w);
        return null;
    }

    public Object visit(BinaryExpr n, Writer w) {
        n.getLeft().accept(this, w);
        w.append(" ");
        w.append(n.getOperator().asString());
        w.append(" ");
        n.getRight().accept(this, w);
        return null;
    }

    public Object visit(UnaryExpr n, Writer w) {
        if (n.isPostfix()) {
            n.getExpression().accept(this, w);
            w.append(n.getOperator().asString());
        } else {
            w.append(n.getOperator().asString());
            n.getExpression().accept(this, w);
        }
        return null;
    }

    public Object visit(NullLiteralExpr n, Writer w) {
        w.append("nullptr");
        return null;
    }

    public Object visit(IntegerLiteralExpr n, Writer w) {
        w.append(n.getValue());
        return null;
    }

    @Override
    public Object visit(CharLiteralExpr n, Writer w) {
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
    public Object visit(CastExpr n, Writer w) {
        w.append("(");
        CType type = typeVisitor.visitType(n.getType(), method);
        w.append(type);
        /*if (type.isPointer()) {
            w.append("*");
        }*/
        w.append(")");
        n.getExpression().accept(this, w);
        return null;
    }

    @Override
    public Object visit(EnclosedExpr n, Writer w) {
        w.append("(");
        n.getInner().accept(this, w);
        w.append(")");
        return null;
    }

    @Override
    public Object visit(LongLiteralExpr n, Writer w) {
        String str = n.getValue();
        if (str.endsWith("L")) {
            str = str.substring(0, str.length() - 1);
        }
        w.append(str);
        return null;
    }

    public Object visit(StringLiteralExpr n, Writer w) {
        w.append("new java::lang::String(\"");
        w.append(n.getValue());
        w.append("\")");
        return null;
    }

    @Override
    public Object visit(BooleanLiteralExpr n, Writer w) {
        if (n.getValue()) {
            w.append("true");
        } else {
            w.append("false");
        }
        return null;
    }

    //new int[]{...} or new int[5]
    public Object visit(ArrayCreationExpr n, Writer w) {
        if (n.getInitializer().isPresent()) {
            //just print values,ignore new type[]... because c++ allows it
            n.getInitializer().get().accept(this, w);
        } else {
            //only dimensions
            w.append("new ");
            CType typeName = typeVisitor.visitType(n.getElementType(), method).copy();
            typeName.arrayLevel = n.getLevels().size();
            w.append(typeName.toString());
            w.append("(");
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
    public Object visit(ArrayInitializerExpr n, Writer w) {
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

    public Object visit(NameExpr n, Writer w) {
        w.append(n.getNameAsString());
        return null;
    }
}
