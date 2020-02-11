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

public class MethodVisitor extends GenericVisitorAdapter<Object, Nodew> {
    public CMethod method;
    public CHeader header;
    public Converter converter;
    boolean hasReturn = false, hasThrow = false, hasBreak = false;
    StatementVisitor statementVisitor;
    ExprVisitor exprVisitor;

    public MethodVisitor(Converter converter, CHeader header) {
        this.header = header;
        this.converter = converter;
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




}
