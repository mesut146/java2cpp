package com.mesut.j2cpp.visitor;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.Type;
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
    boolean hasReturn = false, hasThrow = false, hasBreak = false;
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
        hasReturn = true;
        w.append("return");
        if (n.getExpression().isPresent()) {
            w.append(" ");
            n.getExpression().get().accept(exprVisitor, w);
        }
        w.append(";");
        return null;
    }

    public Object visit(TryStmt n, Writer w) {

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
            Writer fin = new Writer();
            //tryRet=hasReturn;hasReturn=false;
            n.getFinallyBlock().get().accept(this, fin);
            //finRet=hasReturn;hasReturn=false;
            w.append("bool tryReturned=true,finReturned=true;");
            w.line(type.toString()).append("* res_tryBlock=with_finally<" + type.toString() + ">([&](){");
            w.up();
            w.line("try");
            Writer tn = new Writer();
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
                    n.getResources().get(i).accept(exprVisitor, w);
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

    void makeCatch(NodeList<CatchClause> list, Writer w) {
        for (CatchClause cc : list) {
            Parameter parameter = cc.getParameter();
            if (parameter.getType().isUnionType()) {
                for (Type type : parameter.getType().asUnionType().getElements()) {
                    w.append("catch(");
                    w.append((CType) type.accept(typeVisitor, new Writer()));
                    w.append(" ");
                    w.append(parameter.getNameAsString());
                    w.append(")");
                    cc.getBody().accept(this, w);
                }
            } else {
                w.append("catch(");
                w.append((CType) parameter.getType().accept(typeVisitor, new Writer()));
                w.append(" ");
                w.append(parameter.getNameAsString());
                w.append(")");
                cc.getBody().accept(this, w);
            }

        }
    }

    void makeLambda(BlockStmt n, Writer w) {
        w.append("auto finally=[&]()");
        n.accept(this, w);
        w.append(";");
    }

    public Object visit(ThrowStmt n, Writer w) {
        hasThrow = true;
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
