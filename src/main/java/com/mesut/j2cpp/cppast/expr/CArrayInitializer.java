package com.mesut.j2cpp.cppast.expr;

import com.mesut.j2cpp.cppast.CExpression;

import java.util.ArrayList;
import java.util.List;

//more like std::initializer_list
public class CArrayInitializer extends CExpression {

    public List<CExpression> expressions = new ArrayList<>();

    @Override
    public void print() {
        append("{");
        for (int i = 0; i < expressions.size(); i++) {
            append(expressions.get(i).toString());
            if (i < expressions.size() - 1) {
                append(", ");
            }
        }
        append("}");
    }
}
