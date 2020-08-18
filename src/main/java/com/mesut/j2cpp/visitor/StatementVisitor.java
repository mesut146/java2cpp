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
        this.exprVisitor.setMethod(method);
        //this.w = method.bodyWriter;
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
    public boolean visit(SwitchStatement node) {
        w.append("switch(");
        node.getExpression().accept(exprVisitor);
        w.appendln("){");
        w.up();
        for (Statement statement : (List<Statement>) node.statements()) {
            //cases
            //System.out.println("st="+statement+" "+statement.getClass());
            statement.accept(this);
        }
        w.appendln("}");
        w.down();
        return false;
    }

    @Override
    public boolean visit(SwitchCase node) {
        if (node.isDefault()) {
            w.appendln("default:");
        }
        else {
            w.append("case ");
            node.getExpression().accept(exprVisitor);
            w.appendln(":");
        }
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
            //no finally stmt just print it directly
            helper.no_finally(n, w);
        }
        return false;
    }

    @Override
    public boolean visit(ThrowStatement n) {
        w.append("throw ");
        n.getExpression().accept(exprVisitor);
        w.append(";");
        return false;
    }

    //type name=value,name2=value2...
    @Override
    public boolean visit(VariableDeclarationStatement node) {
        //w.append("var.decl.stmt ");
        boolean first = true;
        CType type = typeVisitor.visitType(node.getType(), method);
        type.isTemplate = false;
        for (VariableDeclarationFragment frag : (List<VariableDeclarationFragment>) node.fragments()) {
            if (first) {
                first = false;
                w.append(type);
                w.append(" ");
            }
            else {
                w.append(",");
            }
            w.append(frag.getName().getIdentifier());
            if (frag.getInitializer() != null) {
                w.append(" = ");
                frag.getInitializer().accept(exprVisitor);
            }
        }
        w.append(";");
        return false;
    }


}
