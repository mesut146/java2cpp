package com.mesut.j2cpp.visitor;

import com.mesut.j2cpp.ast.CMethod;
import com.mesut.j2cpp.cppast.CExpression;
import com.mesut.j2cpp.cppast.stmt.CBlockStatement;
import com.mesut.j2cpp.cppast.stmt.CExpressionStatement;
import com.mesut.j2cpp.cppast.stmt.CTryStatement;
import org.eclipse.jdt.core.dom.*;

import java.util.List;

public class TryHelper {

    SourceVisitor visitor;

    public TryHelper(SourceVisitor visitor) {
        this.visitor = visitor;
    }

    public CTryStatement with_finally(TryStatement node) {
        CTryStatement tryStatement = new CTryStatement();
        tryStatement.body = (CBlockStatement) visitor.visit(node.getBody(), null);
        CMethod method = visitor.method;
        if (method.decl.isCons || method.getType().isVoid()) {
            return with_void(node);
        }/*
        String retStr = "if(0){ return";
        CType type = method.getType();
        if (type.isPrim()) {
            retStr += " 0";
        }
        else {
            retStr += " nullptr";
        }
        retStr += "; }";
        w.append("bool tryReturned = true,finallyReturned = true;");
        w.line(type.toString()).append(" result_tryBlock = valued_finally<" + type + ">([&](){");


        node.getBody().accept(statementVisitor);

        w.appendIndent(try_writer);
        if (node.catchClauses().isEmpty()) {
            w.line("catch(int x){}");
            CCatchClause catchClause = new CCatchClause();
            catchClause
        }
        else {
            printCatch(node.catchClauses(), w);
        }
        w.line(retStr);
        w.line("tryReturned = false;");

        w.lineln("},[&](){");


        node.getFinally().accept(statementVisitor);
        w.appendIndent(fin);
        w.line(retStr);
        w.line("finReturned = false;");
        w.down();
        w.line("});");
        if (!method.decl.isCons && !method.getType().isVoid()) {
            CIfStatement ifStatement = new CIfStatement();
            CInfixExpression infixExpression = new CInfixExpression();
            infixExpression.left = new CName("");
            infixExpression.right = new CName("");
            infixExpression.operator = "||";
            ifStatement.condition = infixExpression;
            ifStatement.thenStatement = new CReturnStatement(new CName("result_tryBlock"));
        }*/
        return tryStatement;
    }

    public CTryStatement with_void(TryStatement node) {
        CTryStatement tryStatement = new CTryStatement();
        //lambda
        /*
        w.line("void_finally([&](){");

        w.line("try");
        Writer try_writer = new Writer();

        node.getBody().accept(statementVisitor);
        w.appendIndent(try_writer);
        if (node.catchClauses().isEmpty()) {
            w.line("catch(int x){}");
        }
        else {
            printCatch(node.catchClauses(), w);
        }
        w.down();
        w.lineln("},[&](){");
        w.up();
        Writer fin = new Writer();

        node.getFinally().accept(statementVisitor);
        w.appendIndent(fin);
        w.down();
        w.line("});");*/
        return tryStatement;
    }

    public CTryStatement no_finally(TryStatement node) {
        CTryStatement tryStatement = new CTryStatement();
        tryStatement.body = (CBlockStatement) visitor.visit(node.getBody(), null);
        int len = node.resources().size();
        if (len > 0) {
            for (int i = 0; i < len; i++) {
                Object obj = node.resources().get(i);
                if (obj instanceof Name) {

                }
                else {//expr
                    CExpression expr = (CExpression) visitor.visit((Expression) obj, null);
                    tryStatement.body.statements.add(0, new CExpressionStatement(expr));
                }
            }
        }
        printCatch(node.catchClauses());
        return tryStatement;
    }

    void printCatch(List list) {
        for (Object obj : list) {
            CatchClause cc = (CatchClause) obj;

            SingleVariableDeclaration exc = cc.getException();

            /*Parameter parameter = cc.getParameter();
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
            }*/

        }
    }
}
