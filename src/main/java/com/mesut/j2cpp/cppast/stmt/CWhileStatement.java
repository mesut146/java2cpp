package com.mesut.j2cpp.cppast.stmt;

import com.mesut.j2cpp.cppast.CExpression;
import com.mesut.j2cpp.cppast.CStatement;
import com.mesut.j2cpp.util.PrintHelper;

public class CWhileStatement extends CStatement {
    public CExpression expression;
    public CStatement statement;

    public void setStatement(CStatement statement) {
        this.statement = statement;
    }

    @Override
    public String toString() {
        getScope(expression, statement);
        if (statement == null) {
            return String.format("while(%s);", expression);
        }
        return String.format("while(%s)%s", expression, PrintHelper.strBody(statement));
    }
}
