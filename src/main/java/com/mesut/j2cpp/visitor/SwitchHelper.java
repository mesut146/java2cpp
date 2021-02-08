package com.mesut.j2cpp.visitor;

import com.mesut.j2cpp.Logger;
import com.mesut.j2cpp.ast.CName;
import com.mesut.j2cpp.ast.CType;
import com.mesut.j2cpp.cppast.CExpression;
import com.mesut.j2cpp.cppast.CStatement;
import com.mesut.j2cpp.cppast.expr.CFieldAccess;
import com.mesut.j2cpp.cppast.expr.CInfixExpression;
import com.mesut.j2cpp.cppast.expr.CMethodInvocation;
import com.mesut.j2cpp.cppast.literal.CCharacterLiteral;
import com.mesut.j2cpp.cppast.literal.CNumberLiteral;
import com.mesut.j2cpp.cppast.stmt.*;
import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.List;

public class SwitchHelper {

    public boolean isEnum = false;
    SourceVisitor visitor;
    List<Statement> statements;
    CExpression left;
    int i;//statement index

    public SwitchHelper(SourceVisitor visitor) {
        this.visitor = visitor;
    }

    CExpression ordinal(CExpression expression) {
        if (expression instanceof CNumberLiteral || expression instanceof CCharacterLiteral) {
            return expression;
        }
        CMethodInvocation methodInvocation = new CMethodInvocation();
        methodInvocation.isArrow = true;
        methodInvocation.scope = expression;
        methodInvocation.name = new CName("ordinal");
        return methodInvocation;
    }

    public List<CStatement> makeIfElse(SwitchStatement node) {
        List<CStatement> result = new ArrayList<>();
        CExpression expression = (CExpression) visitor.visitExpr(node.getExpression(), null);
        //for enums ve call ordinal method store result in a var and make regular if else's

        if (!(expression instanceof CFieldAccess) && !(expression instanceof CName) || isEnum) {
            CType type;
            if (isEnum) {
                type = new CType("int");
                expression = ordinal(expression);
            }
            else {
                type = visitor.typeVisitor.fromBinding(node.getExpression().resolveTypeBinding(), visitor.clazz);
            }

            CVariableDeclarationStatement ord = new CVariableDeclarationStatement();
            CVariableDeclarationFragment frag = new CVariableDeclarationFragment();
            ord.type = type;
            left = frag.name = new CName("_val");
            frag.initializer = expression;
            ord.fragments.add(frag);
            result.add(ord);
        }
        else {
            left = expression;
        }

        statements = node.statements();

        CIfStatement lastIf = null, firstIf = null;

        CBlockStatement defaultStmt = null;

        for (i = 0; i < statements.size(); ) {
            Statement statement = statements.get(i);
            if (statement instanceof SwitchCase) {
                SwitchCase switchCase = (SwitchCase) statement;
                if (switchCase.isDefault()) {
                    i++;//skip default
                    defaultStmt = collectStatements();
                }
                else {
                    List<CExpression> cases = collectCases();
                    CIfStatement ifStatement = new CIfStatement();

                    ifStatement.condition = makeCondition(cases);
                    ifStatement.thenStatement = collectStatements();

                    if (firstIf == null) {
                        firstIf = ifStatement;
                        lastIf = firstIf;
                    }
                    else {
                        lastIf.elseStatement = ifStatement;
                        lastIf = ifStatement;
                    }
                }
            }
            else {
                throw new RuntimeException("invalid switch statement");
            }
        }
        if (lastIf == null) {
            Logger.log("invalid switch in " + visitor.clazz.getType().basicForm());
        }
        else {
            lastIf.elseStatement = defaultStmt;
        }
        result.add(firstIf);
        return result;
    }

    //scan merged cases and collect expressions
    List<CExpression> collectCases() {
        List<CExpression> expr = new ArrayList<>();
        for (; i < statements.size(); i++) {
            Statement statement = statements.get(i);
            if (statement instanceof SwitchCase) {
                SwitchCase switchCase = (SwitchCase) statement;
                if (switchCase.isDefault()) {
                    //special
                }
                else {
                    expr.add((CExpression) visitor.visitExpr(switchCase.getExpression(), null));
                }

            }
            else {
                break;
            }
        }
        return expr;
    }

    CBlockStatement collectStatements() {
        CBlockStatement blockStatement = new CBlockStatement();
        for (; i < statements.size(); i++) {
            Statement statement = statements.get(i);
            if (statement instanceof SwitchCase) {
                break;
            }
            else if (!(statement instanceof BreakStatement)) {//skip breaks since we use if's
                blockStatement.addStatement((CStatement) visitor.visitExpr(statement, null));
            }
        }
        if (blockStatement.statements.size() == 1 && blockStatement.statements.get(0) instanceof CBlockStatement) {
            return (CBlockStatement) blockStatement.statements.get(0);
        }
        return blockStatement;
    }

    //given a , b , c
    //make left==a || left==b || ...
    CExpression makeCondition(List<CExpression> list) {
        if (list.size() == 1) {
            return makeInfix(left, list.get(0), "==");
        }
        else {
            CInfixExpression firstOr = makeInfix(null, null, "||");
            CInfixExpression lastOr = firstOr;

            for (int j = 0; j < list.size(); j++) {
                CInfixExpression infixExpression = makeInfix(left, list.get(j), "==");
                if (j == 0) {
                    firstOr.left = infixExpression;
                }
                else {
                    if (j == list.size() - 1) {
                        lastOr.right = infixExpression;
                    }
                    else {
                        lastOr.left = infixExpression;
                    }
                    //lastOr.right = infixExpression;
                }
                if (j < list.size() - 2) {
                    lastOr.right = makeInfix(null, null, "||");
                    lastOr = (CInfixExpression) lastOr.right;
                }
            }
            return firstOr;
        }
    }

    CInfixExpression makeInfix(CExpression left, CExpression right, String op) {
        CInfixExpression infixExpression = new CInfixExpression();
        infixExpression.left = left;
        infixExpression.operator = op;

        if ("==".equals(op)) {
            right = ordinal(right);
        }
        infixExpression.right = right;
        return infixExpression;
    }

}
