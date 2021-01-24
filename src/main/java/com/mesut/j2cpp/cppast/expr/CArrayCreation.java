package com.mesut.j2cpp.cppast.expr;

import com.mesut.j2cpp.Config;
import com.mesut.j2cpp.ast.CType;
import com.mesut.j2cpp.cppast.CExpression;
import com.mesut.j2cpp.util.ArrayHelper;

import java.util.ArrayList;
import java.util.List;

//new type[d1][d2]
public class CArrayCreation extends CExpression {

    public CType type;
    public List<CExpression> dimensions = new ArrayList<>();

    public CArrayCreation(CType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        if (Config.use_vector) {
            return "new " + ArrayHelper.printRight(dimensions, type, 0);
        }
        type.isPointer = false;
        return "new " + type + "()";
    }
}
