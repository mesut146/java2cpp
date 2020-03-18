package com.mesut.j2cpp.visitor;

import com.mesut.j2cpp.Converter;
import com.mesut.j2cpp.Writer;
import com.mesut.j2cpp.ast.CHeader;
import com.mesut.j2cpp.ast.CMethod;
import com.mesut.j2cpp.ast.CType;
import org.eclipse.jdt.core.dom.*;

import java.util.Iterator;
import java.util.List;

//visit statements (ones end with ;)
public class StatementVisitor extends GenericVisitor<Object, Writer> {

    public CMethod method;
    public CHeader header;
    public Converter converter;
    ExprVisitor exprVisitor;
    TypeVisitor typeVisitor;
    //Writer w;

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

    @Override
    public Object visit(Block n, Writer w) {
        w.firstBlock = true;
        w.appendln("{");
        w.up();

        for (Statement statement : (List<Statement>) n.statements()) {
            statement.accept(this);
            w.println();
        }
        w.down();
        w.append("}");
        return false;
    }

    @Override
    public Object visit(ExpressionStatement n, Writer w) {
        n.getExpression().accept(exprVisitor);
        w.append(";");
        return false;
    }

    @Override
    public Object visit(IfStatement n, Writer w) {
        w.append("if(");
        n.getExpression().accept(exprVisitor);//condition
        w.append(")");
        block(w, n.getThenStatement());
        if (n.getElseStatement() != null) {
            w.append("else");
            block(w, n.getElseStatement());
        }
        return false;
    }

    //handles indention for statements
    void block(Writer w, Statement statement) {
        if (statement instanceof Block) {
            statement.accept(this);
        }
        else {
            w.println();
            w.up();
            statement.accept(this);
            w.down();
        }
    }

    @Override
    public Object visit(WhileStatement n, Writer w) {
        w.append("while(");
        n.getExpression().accept(exprVisitor);
        w.append(") ");
        block(w, n.getBody());
        return false;
    }

    @Override
    public Object visit(ForStatement n, Writer w) {
        w.append("for(");

        for (Iterator<Expression> iterator = n.initializers().iterator(); iterator.hasNext(); ) {
            iterator.next().accept(exprVisitor);

            if (iterator.hasNext()) {
                w.append(",");
            }
        }
        w.append(";");
        if (n.getExpression() != null) {
            n.getExpression().accept(exprVisitor);
        }
        w.append(";");
        for (Iterator<Expression> iterator = n.updaters().iterator(); iterator.hasNext(); ) {
            iterator.next().accept(exprVisitor);

            if (iterator.hasNext()) {
                w.append(",");
            }
        }
        w.append(")");
        block(w, n.getBody());
        return false;
    }

    @Override
    public Object visit(EnhancedForStatement n, Writer w) {
        w.append("for(");
        n.getParameter().accept(exprVisitor);
        w.append(":");
        n.getExpression().accept(exprVisitor);
        w.append(")");
        block(w, n.getBody());
        return false;
    }

    @Override
    public Object visit(ReturnStatement n, Writer w) {
        w.append("return");
        if (n.getExpression() != null) {
            w.append(" ");
            n.getExpression().accept(exprVisitor);
        }
        w.append(";");
        return false;
    }

    @Override
    public Object visit(TryStatement n, Writer w) {
        TryHelper helper = new TryHelper(exprVisitor, this);

        if (n.getFinally() != null) {
            header.addRuntime();
            helper.with_finally(n, w);
        }
        else {
            //no finally stmt just print it directly
            helper.no_finally(n, w);
        }
        return false;
    }

    @Override
    public Object visit(ThrowStatement n, Writer w) {
        w.append("throw ");
        n.getExpression().accept(exprVisitor);
        return false;
    }
    /*
    public Object visit(ExplicitConstructorInvocationStmt n, Writer w) {
        Writer p = new Writer();
        Call c = new Call();
        c.isThis = n.isThis();
        if (n.isThis()) {
            p.line(method.getName());
        }
        else {
            if (n.getExpression().isPresent()) {
                return null;
            }
            else {
                p.line(method.getParent().base.get(0).type);
            }
        }
        exprVisitor.args(n.getArguments(), p);
        c.str = p.toString();
        method.call = c;
        return null;
    }*/
}
