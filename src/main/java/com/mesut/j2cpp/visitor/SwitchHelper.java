package com.mesut.j2cpp.visitor;

import com.mesut.j2cpp.cppast.CExpression;
import com.mesut.j2cpp.cppast.CStatement;
import com.mesut.j2cpp.cppast.expr.CInfixExpression;
import com.mesut.j2cpp.cppast.stmt.CIfStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;

import java.util.ArrayList;
import java.util.List;

public class SwitchHelper {

    SourceVisitor visitor;

    public SwitchHelper(SourceVisitor visitor) {
        this.visitor = visitor;
    }

    void makeIfElse(SwitchStatement node) {
        Expression expression = node.getExpression();
        CExpression left = (CExpression) visitor.visit(expression, null);

        boolean inCase = false;
        List<Statement> list = node.statements();

        List<CStatement> expressions = new ArrayList<>();

        CStatement lastIf;

        for (int i = 0; i < list.size(); i++) {
            Statement statement = list.get(i);
            if (statement instanceof SwitchCase) {
                SwitchCase switchCase = (SwitchCase) statement;
                if (switchCase.isDefault()) {

                }
                else {
                    CIfStatement ifStatement = new CIfStatement();
                    lastIf = ifStatement;
                    collectCases(list, i);
                    if (i == 0) {
                        List<CExpression> ifList = new ArrayList();
                        ifList.add((CExpression) visitor.visit(switchCase.getExpression(), null));


                        ifStatement.condition = makeCondition(ifList, left);
                    }
                    else {

                    }
                    //CIfStatement ifStatement = new CIfStatement();
                }
                inCase = true;
            }
            else {

            }
        }
    }

    CExpression makeCondition(List<CExpression> list, CExpression left) {
        if (list.size() == 1) {
            return makeInfix(left, list.get(0), "==");
        }
        else {
            CInfixExpression orList = makeInfix(null, null, "||");
            for (CExpression expression : list) {
                makeInfix(left, expression, "==");
            }
            return orList;
        }

    }

    CInfixExpression makeInfix(CExpression left, CExpression right, String op) {
        CInfixExpression infixExpression = new CInfixExpression();
        infixExpression.left = left;
        infixExpression.operator = op;
        infixExpression.right = right;
        return infixExpression;
    }

    //scan merged cases and collect expressions
    List<CExpression> collectCases(List<Statement> list, int i) {
        List<CExpression> expr = new ArrayList<>();
        for (; i < list.size(); i++) {
            Statement statement = list.get(i);
            if (statement instanceof SwitchCase) {
                SwitchCase switchCase = (SwitchCase) statement;
                if (switchCase.isDefault()) {
                    //special
                }
                else {
                    expr.add((CExpression) visitor.visit(switchCase.getExpression(), null));
                }

            }
            else {
                break;
            }
        }
        return expr;
    }
}
