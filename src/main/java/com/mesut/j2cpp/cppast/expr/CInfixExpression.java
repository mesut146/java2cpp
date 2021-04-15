package com.mesut.j2cpp.cppast.expr;

import com.mesut.j2cpp.cppast.CExpression;

import java.util.ArrayList;
import java.util.List;

// a + b
public class CInfixExpression extends CExpression {
    public CExpression left;
    public CExpression right;
    public String operator;
    public List<CExpression> other = new ArrayList<>();

    public CInfixExpression() {
    }

    public CInfixExpression(CExpression left, CExpression right, String operator) {
        this.left = left;
        this.right = right;
        this.operator = operator;
    }

    @Override
    public String toString() {
        getScope(left, right);
        getScope(other);
        if (other.isEmpty()) {
            return left + " " + operator + " " + right;
        }
        else {
            StringBuilder sb = new StringBuilder();
            sb.append(left).append(" ").append(operator).append(" ").append(right);
            for (CExpression e : other) {
                sb.append(" ").append(operator).append(" ").append(e);
            }
            return sb.toString();
        }
    }
}
