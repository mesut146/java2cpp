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
public class StatementVisitor extends ASTVisitor {

    public CMethod method;
    public CHeader header;
    public Converter converter;
    ExprVisitor exprVisitor;
    TypeVisitor typeVisitor;
    Writer w;

    public StatementVisitor(Converter converter, CHeader header, ExprVisitor exprVisitor, TypeVisitor typeVisitor) {
        this.converter = converter;
        this.header = header;
        this.exprVisitor = exprVisitor;
        this.typeVisitor = typeVisitor;
    }

    public void setMethod(CMethod method) {
        this.method = method;
        this.w = method.bodyWriter;
        this.exprVisitor.setMethod(method);
    }

    public Object visit(SimpleName n, Writer w) {
        return new CType(n.getIdentifier());
    }

    @Override
    public boolean visit(Block n) {
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
    public boolean visit(ExpressionStatement n) {
        n.getExpression().accept(exprVisitor);
        w.append(";");
        return false;
    }

    @Override
    public boolean visit(IfStatement n) {
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
    public boolean visit(WhileStatement n) {
        w.append("while(");
        n.getExpression().accept(exprVisitor);
        w.append(") ");
        block(w, n.getBody());
        return false;
    }

    @Override
    public boolean visit(ForStatement n) {
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
    public boolean visit(EnhancedForStatement n) {
        w.append("for(");
        n.getParameter().accept(exprVisitor);
        w.append(":");
        n.getExpression().accept(exprVisitor);
        w.append(")");
        block(w, n.getBody());
        return false;
    }

    @Override
    public boolean visit(ReturnStatement n) {
        w.append("return");
        if (n.getExpression() != null) {
            w.append(" ");
            n.getExpression().accept(exprVisitor);
        }
        w.append(";");
        return false;
    }

    @Override
    public boolean visit(TryStatement n) {
        TryHelper helper = new TryHelper(exprVisitor, this);

        if (n.getFinally() != null) {
            header.addRuntime();
            helper.with_finally(n, w);
        }
        else {
            //no finnaly stmt just print it directly
            helper.no_finally(n, w);
        }
        return false;
    }

    @Override
    public boolean visit(ThrowStatement n) {
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