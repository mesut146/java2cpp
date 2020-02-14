package com.mesut.j2cpp.visitor;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;
import com.mesut.j2cpp.Converter;
import com.mesut.j2cpp.Writer;
import com.mesut.j2cpp.ast.CHeader;
import com.mesut.j2cpp.ast.CMethod;
import com.mesut.j2cpp.ast.CType;
import com.mesut.j2cpp.ast.Call;

import java.util.Iterator;

//visit statements (ones end with ;)
public class StatementVisitor extends GenericVisitorAdapter<Object, Writer> {

    public CMethod method;
    public CHeader header;
    public Converter converter;
    //boolean hasReturn = false, hasThrow = false, hasBreak = false;
    ExprVisitor exprVisitor;
    TypeVisitor typeVisitor;

    public StatementVisitor(Converter converter, CHeader header, ExprVisitor exprVisitor, TypeVisitor typeVisitor) {
        this.converter = converter;
        this.header = header;
        this.exprVisitor = exprVisitor;
        this.typeVisitor = typeVisitor;
    }

    public void setMethod(CMethod method) {
        this.method = method;
        this.exprVisitor.setMethod(method);
    }

    public Object visit(SimpleName n, Writer w) {
        return new CType(n.getIdentifier());
    }

    public Object visit(BlockStmt n, Writer w) {
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

    public Object visit(ExpressionStmt n, Writer w) {
        n.getExpression().accept(exprVisitor, w);
        w.append(";");
        return null;
    }

    public Object visit(IfStmt n, Writer w) {
        w.append("if(");
        n.getCondition().accept(exprVisitor, w);
        w.append(")");
        block(w, n.getThenStmt());
        if (n.getElseStmt().isPresent()) {
            w.append("else");
            block(w, n.getElseStmt().get());
        }
        return null;
    }

    void block(Writer w, Statement statement) {
        if (statement.isBlockStmt()) {
            statement.accept(this, w);
        } else {
            w.println();
            w.up();
            statement.accept(this, w);
            w.down();
        }
    }

    public Object visit(WhileStmt n, Writer w) {
        w.append("while(");
        n.getCondition().accept(exprVisitor, w);
        w.append(") ");
        block(w, n.getBody());
        return null;
    }

    public Object visit(ForStmt n, Writer w) {
        w.append("for(");
        for (Iterator<Expression> iterator = n.getInitialization().iterator(); iterator.hasNext(); ) {
            iterator.next().accept(exprVisitor, w);
            if (iterator.hasNext()) {
                w.append(",");
            }
        }
        w.append(";");
        if (n.getCompare().isPresent()) {
            n.getCompare().get().accept(exprVisitor, w);
        }
        w.append(";");
        for (Iterator<Expression> iterator = n.getUpdate().iterator(); iterator.hasNext(); ) {
            iterator.next().accept(exprVisitor, w);
            if (iterator.hasNext()) {
                w.append(",");
            }
        }
        w.append(")");
        block(w, n.getBody());
        return null;
    }

    public Object visit(ForEachStmt n, Writer w) {
        w.append("for(");
        n.getVariable().accept(exprVisitor, w);
        w.append(":");
        n.getIterable().accept(exprVisitor, w);
        w.append(")");
        block(w, n.getBody());
        return null;
    }

    public Object visit(ReturnStmt n, Writer w) {
        w.append("return");
        if (n.getExpression().isPresent()) {
            w.append(" ");
            n.getExpression().get().accept(exprVisitor, w);
        }
        w.append(";");
        return null;
    }

    public Object visit(TryStmt n, Writer w) {
        TryHelper helper = new TryHelper(exprVisitor, this);
        if (n.getFinallyBlock().isPresent()) {
            header.addRuntime();
            helper.with_finally(n, w);
        } else {
            //no finnaly stmt just print it directly
            helper.no_finally(n, w);
        }
        return null;
    }

    public Object visit(ThrowStmt n, Writer w) {
        w.append("throw ");
        n.getExpression().accept(exprVisitor, w);
        return null;
    }

    public Object visit(ExplicitConstructorInvocationStmt n, Writer w) {
        Writer p = new Writer();
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
        exprVisitor.args(n.getArguments(), p);
        c.str = p.toString();
        method.call = c;
        return null;
    }
}
