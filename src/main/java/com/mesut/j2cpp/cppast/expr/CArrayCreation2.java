package com.mesut.j2cpp.cppast.expr;

import com.mesut.j2cpp.Config;
import com.mesut.j2cpp.ast.CType;
import com.mesut.j2cpp.cppast.CExpression;
import com.mesut.j2cpp.util.VectorHelper;

import java.util.ArrayList;
import java.util.List;

//new type[d1][d2]
public class CArrayCreation2 extends CExpression {

    public CType type;
    public List<CExpression> dimensions = new ArrayList<>();

    public CArrayCreation2(CType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        if (Config.use_vector) {
            return "new " + VectorHelper.printRight(dimensions, type, 0);
        }
        type.isPointer = false;
        return "new " + type + "()";
    }
}
