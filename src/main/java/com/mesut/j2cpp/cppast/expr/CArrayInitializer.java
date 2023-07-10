package com.mesut.j2cpp.cppast.expr;

import com.mesut.j2cpp.cppast.CExpression;
import com.mesut.j2cpp.util.PrintHelper;

import java.util.ArrayList;
import java.util.List;

//more like std::initializer_list
public class CArrayInitializer extends CExpression {

    public List<CExpression> expressions = new ArrayList<>();

    public CArrayInitializer(List<CExpression> expressions) {
        this.expressions = expressions;
    }

    public CArrayInitializer() {
    }

    @Override
    public String toString() {
        getScope(expressions);
        return "{" + PrintHelper.joinStr(expressions, ", ") + "}";
    }
}
