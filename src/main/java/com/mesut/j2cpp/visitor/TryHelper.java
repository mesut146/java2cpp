package com.mesut.j2cpp.visitor;

import com.mesut.j2cpp.ast.CMethod;
import com.mesut.j2cpp.ast.CName;
import com.mesut.j2cpp.ast.CType;
import com.mesut.j2cpp.ast.CUnionType;
import com.mesut.j2cpp.cppast.CCatchClause;
import com.mesut.j2cpp.cppast.CExpression;
import com.mesut.j2cpp.cppast.CLambdaExpression;
import com.mesut.j2cpp.cppast.CLineCommentStatement;
import com.mesut.j2cpp.cppast.expr.CMethodInvocation;
import com.mesut.j2cpp.cppast.stmt.CBlockStatement;
import com.mesut.j2cpp.cppast.stmt.CExpressionStatement;
import com.mesut.j2cpp.cppast.stmt.CSingleVariableDeclaration;
import com.mesut.j2cpp.cppast.stmt.CTryStatement;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.TryStatement;

import java.util.List;

@SuppressWarnings("unchecked")
public class TryHelper {

    SourceVisitor visitor;

    public TryHelper(SourceVisitor visitor) {
        this.visitor = visitor;
    }

    public CBlockStatement with_finally(TryStatement node) {
        CBlockStatement blockStatement = new CBlockStatement();
        blockStatement.addStatement(new CLineCommentStatement("try_catch"));
        CTryStatement tryStatement = new CTryStatement();

        printCatches(node.catchClauses(), tryStatement);

        tryStatement.body = (CBlockStatement) visitor.visit(node.getBody(), null);

        CMethod method = visitor.method;
        if (method.isCons || method.type.isVoid()) {
            with_void(node, tryStatement, blockStatement);
        }
        return blockStatement;
    }

    private void with_void(TryStatement node, CTryStatement tryStatement, CBlockStatement blockStatement) {
        //make lambda
        CLambdaExpression lambdaExpression = new CLambdaExpression();
        lambdaExpression.byReference = true;
        CBlockStatement lambdaBlock = new CBlockStatement();
        lambdaBlock.addStatement(tryStatement);
        lambdaExpression.body = lambdaBlock;
        //handle catches


        //call helper
        CMethodInvocation invocation = new CMethodInvocation();
        invocation.isArrow = false;
        invocation.name = new CName("helper");
        invocation.arguments.add(lambdaExpression);

        blockStatement.addStatement(new CExpressionStatement(invocation));
        blockStatement.addStatement(new CLineCommentStatement("finally"));
        blockStatement.addStatement(getFinally(node));
        //call finally


    }


    CBlockStatement getFinally(TryStatement node) {
        return (CBlockStatement) visitor.visit(node.getFinally(), null);
    }

    public CTryStatement no_finally(TryStatement node) {
        CTryStatement tryStatement = new CTryStatement();
        printCatches(node.catchClauses(), tryStatement);
        tryStatement.body = (CBlockStatement) visitor.visit(node.getBody(), null);
        int len = node.resources().size();
        if (len > 0) {
            for (int i = 0; i < len; i++) {
                Object obj = node.resources().get(i);
                if (obj instanceof Name) {
                    throw new RuntimeException("try with name expression: " + obj);
                }
                else {//expr
                    CExpression expr = (CExpression) visitor.visitExpr((Expression) obj, null);
                    tryStatement.body.addStatement(0, new CExpressionStatement(expr));
                }
            }
        }

        return tryStatement;
    }

    void printCatches(List<CatchClause> list, CTryStatement tryStatement) {
        if (list.isEmpty()) {
            addCatch(tryStatement);//c++ requires a catch clause
        }
        for (CatchClause cc : list) {
            CSingleVariableDeclaration var = (CSingleVariableDeclaration) visitor.visit(cc.getException(), null);
            CBlockStatement body = (CBlockStatement) visitor.visit(cc.getBody(), null);

            if (var.type instanceof CUnionType) {
                //make multiple catch clauses
                for (CType type : ((CUnionType) var.type).types) {
                    CCatchClause catchClause = new CCatchClause();
                    CSingleVariableDeclaration varDecl = new CSingleVariableDeclaration();
                    varDecl.type = type;
                    varDecl.name = var.name;

                    catchClause.expr = varDecl;
                    catchClause.body = body;
                    tryStatement.catchClauses.add(catchClause);
                }
            }
            else {
                CCatchClause catchClause = new CCatchClause();
                catchClause.expr = var;
                catchClause.body = body;
                tryStatement.catchClauses.add(catchClause);
            }


        }
    }

    void addCatch(CTryStatement tryStatement) {

        CCatchClause catchClause = new CCatchClause();
        catchClause.body = new CBlockStatement();
        catchClause.catchAll = true;
        tryStatement.catchClauses.add(catchClause);

    }


}
