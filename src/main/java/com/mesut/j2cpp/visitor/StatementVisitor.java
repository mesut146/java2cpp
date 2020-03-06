package com.mesut.j2cpp.visitor;

import com.mesut.j2cpp.Converter;
import com.mesut.j2cpp.Writer;
import com.mesut.j2cpp.ast.CHeader;
import com.mesut.j2cpp.ast.CMethod;
import com.mesut.j2cpp.ast.CType;
import com.mesut.j2cpp.ast.Call;
import org.eclipse.jdt.core.dom.*;

import java.util.Iterator;

//visit statements (ones end with ;)
public class StatementVisitor extends GenericVisitor<Object, Writer> {

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

    @Override
    public Object visit(Block n, Writer w) {
        w.firstBlock = true;
        w.appendln("{");
        w.up();

        for (Object obj : n.statements()) {
            Statement statement = (Statement) obj;
            visit(statement, w);
            w.println();
        }
        w.down();
        w.append("}");
        return null;
    }

    @Override
    public Object visit(ExpressionStatement n, Writer w) {
        exprVisitor.visit(n.getExpression(), w);
        w.append(";");
        return null;
    }

    @Override
    public Object visit(IfStatement n, Writer w) {
        w.append("if(");
        exprVisitor.visit(n.getExpression(), w);//condition
        w.append(")");
        block(w, n.getThenStatement());
        if (n.getElseStatement() != null) {
            w.append("else");
            block(w, n.getElseStatement());
        }
        return null;
    }

    //handles indention for statements
    void block(Writer w, Statement statement) {
        if (statement instanceof Block) {
            visit(statement, w);
        }
        else {
            w.println();
            w.up();
            visit(statement, w);
            w.down();
        }
    }

    @Override
    public Object visit(WhileStatement n, Writer w) {
        w.append("while(");
        exprVisitor.visit(n.getExpression(), w);
        w.append(") ");
        block(w, n.getBody());
        return null;
    }

    @Override
    public Object visit(ForStatement n, Writer w) {
        w.append("for(");

        for (Iterator<Object> iterator = n.initializers().iterator(); iterator.hasNext(); ) {
            Object obj = iterator.next();
            if (obj instanceof VariableDeclarationExpression) {
                exprVisitor.visit((VariableDeclarationExpression) obj, w);
            }

            if (iterator.hasNext()) {
                w.append(",");
            }
        }
        w.append(";");
        if (n.getExpression() != null) {
            exprVisitor.visit(n.getExpression(), w);
        }
        w.append(";");
        for (Iterator<Object> iterator = n.updaters().iterator(); iterator.hasNext(); ) {
            Object obj = iterator.hasNext();
            if (obj instanceof Expression) {
                exprVisitor.visit((Expression) obj, w);
            }

            if (iterator.hasNext()) {
                w.append(",");
            }
        }
        w.append(")");
        block(w, n.getBody());
        return null;
    }

    /*public Object visit(ForEach n, Writer w) {
        w.append("for(");
        n.getVariable().accept(exprVisitor, w);
        w.append(":");
        n.getIterable().accept(exprVisitor, w);
        w.append(")");
        block(w, n.getBody());
        return null;
    }*/

    @Override
    public Object visit(ReturnStatement n, Writer w) {
        w.append("return");
        if (n.getExpression() != null) {
            w.append(" ");
            exprVisitor.visit(n.getExpression(), w);
        }
        w.append(";");
        return null;
    }

    @Override
    public Object visit(TryStatement n, Writer w) {
        TryHelper helper = new TryHelper(exprVisitor, this);

        if (n.getFinally()!=null) {
            header.addRuntime();
            helper.with_finally(n, w);
        }
        else {
            //no finnaly stmt just print it directly
            helper.no_finally(n, w);
        }
        return null;
    }

    @Override
    public Object visit(ThrowStatement n, Writer w) {
        w.append("throw ");
        exprVisitor.visit(n.getExpression(),w);
        return null;
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
