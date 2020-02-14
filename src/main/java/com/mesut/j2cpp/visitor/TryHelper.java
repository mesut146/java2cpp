package com.mesut.j2cpp.visitor;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.type.Type;
import com.mesut.j2cpp.Writer;
import com.mesut.j2cpp.ast.CMethod;
import com.mesut.j2cpp.ast.CType;

public class TryHelper {
    ExprVisitor exprVisitor;
    StatementVisitor statementVisitor;

    public TryHelper(ExprVisitor exprVisitor, StatementVisitor statementVisitor) {
        this.exprVisitor = exprVisitor;
        this.statementVisitor = statementVisitor;
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public void with_finally(TryStmt node, Writer w) {
        CMethod method = statementVisitor.method;
        if (method.getType().isVoid() || method.isCons) {
            with_void(node, w);
            return;
        }
        String retStr = "if(0){ return";
        CType type = method.type;
        if (type.isPrim()) {
            retStr += " 0";
        } else {
            retStr += " nullptr";
        }
        retStr += "; }";
        w.append("bool tryReturned = true,finallyReturned = true;");
        w.line(type.toString()).append(" result_tryBlock = valued_finally<" + type + ">([&](){");
        w.up();
        w.line("try");
        Writer try_writer = new Writer();
        node.getTryBlock().accept(statementVisitor, try_writer);
        w.appendIndent(try_writer);
        if (node.getCatchClauses().size() == 0) {
            w.line("catch(int x){}");
        } else {
            printCatch(node.getCatchClauses(), w);
        }
        w.line(retStr);
        w.line("tryReturned = false;");
        w.down();
        w.lineln("},[&](){");
        w.up();
        Writer fin = new Writer();
        node.getFinallyBlock().get().accept(statementVisitor, fin);
        w.appendIndent(fin);
        w.line(retStr);
        w.line("finReturned = false;");
        w.down();
        w.line("});");
        if (!method.isCons && !method.type.isVoid()) {
            w.line("if(tryReturned || finallyReturned){");
            w.up();
            w.line("return result_tryBlock;");
            w.down();
            w.line("}");
        }
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public void with_void(TryStmt node, Writer w) {
        //lambda
        w.line("void_finally([&](){");
        w.up();
        w.line("try");
        Writer try_writer = new Writer();
        node.getTryBlock().accept(statementVisitor, try_writer);
        w.appendIndent(try_writer);
        if (node.getCatchClauses().size() == 0) {
            w.line("catch(int x){}");
        } else {
            printCatch(node.getCatchClauses(), w);
        }
        w.down();
        w.lineln("},[&](){");
        w.up();
        Writer fin = new Writer();
        node.getFinallyBlock().get().accept(statementVisitor, fin);
        w.appendIndent(fin);
        w.down();
        w.line("});");
    }

    public void no_finally(TryStmt node, Writer w) {
        w.append("try");
        int len = node.getResources().size();
        if (len > 0) {
            w.append("(");
            for (int i = 0; i < len; i++) {
                node.getResources().get(i).accept(exprVisitor, w);
                if (i < len - 1) {
                    w.append(",");
                }
            }
            w.append(")");
        }
        node.getTryBlock().accept(statementVisitor, w);
        printCatch(node.getCatchClauses(), w);
    }

    void printCatch(NodeList<CatchClause> list, Writer w) {
        for (CatchClause cc : list) {
            Parameter parameter = cc.getParameter();
            if (parameter.getType().isUnionType()) {
                //todo maybe just java::lang:Exception is fine
                //extract union types as separate catch stmt
                for (Type type : parameter.getType().asUnionType().getElements()) {
                    w.append("catch(");
                    w.append(type.accept(statementVisitor.typeVisitor, null));
                    w.append(" ");
                    w.append(parameter.getNameAsString());
                    w.append(")");
                    cc.getBody().accept(statementVisitor, w);
                }
            } else {
                w.append("catch(");
                w.append(parameter.getType().accept(statementVisitor.typeVisitor, null));
                w.append(" ");
                w.append(parameter.getNameAsString());
                w.append(")");
                cc.getBody().accept(statementVisitor, w);
            }

        }
    }
}
