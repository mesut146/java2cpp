package com.mesut.j2cpp.cppast.stmt;

import com.mesut.j2cpp.cppast.CExpression;
import com.mesut.j2cpp.cppast.CStatement;
import com.mesut.j2cpp.util.PrintHelper;

import java.util.ArrayList;
import java.util.List;

public class CSwitch extends CStatement {
    public CExpression expression;
    public List<CStatement> statements = new ArrayList<>();

    @Override
    public String toString() {
        getScope(expression);
        getScope(statements);
        StringBuilder sb = new StringBuilder();
        sb.append("switch(").append(expression).append("){\n");
        for (int i = 0; i < statements.size(); ) {
            CStatement statement = statements.get(i);
            if (statement instanceof CCase) {
                sb.append(statement);
                i++;
            }
            else {
                i++;
                //merge statements so that c++ compiler doesn't complain about var decl
                if (!(statement instanceof CBlockStatement) && !(statement instanceof CReturnStatement)) {
                    CBlockStatement b = new CBlockStatement();
                    b.addStatement(statement);
                    while (i < statements.size()) {
                        if (statements.get(i) instanceof CCase) {
                            break;
                        }
                        b.addStatement(statements.get(i));
                        i++;
                    }
                    statement = b;
                }
                sb.append(PrintHelper.body(statement.toString(), "    "));
            }
            sb.append("\n");
        }
        sb.append("\n}");
        return sb.toString();
    }
}
