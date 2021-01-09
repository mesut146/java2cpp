package com.mesut.j2cpp.ast;

import com.mesut.j2cpp.Config;
import com.mesut.j2cpp.util.ArrayHelper;
import com.mesut.j2cpp.util.VectorHelper;

public class CArrayType extends CType {
    public int dimensions;

    public CArrayType(CType type, int dimensions) {
        this.type = type.type;
        this.ns = type.ns;
        this.typeNames = type.typeNames;
        this.dimensions = dimensions;
    }

    public CType copy() {
        return new CArrayType(this, dimensions);
    }


    @Override
    public String toString() {
        if (Config.use_vector) {
            return VectorHelper.printLeft(dimensions, this);
        }
        else {
            return ArrayHelper.print(dimensions, this);
        }
    }
}
