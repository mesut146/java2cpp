package com.mesut.j2cpp.ast;

import com.mesut.j2cpp.Config;
import com.mesut.j2cpp.util.Helper;
import com.mesut.j2cpp.util.VectorHelper;

public class CArrayType extends CType {
    public int dimensions;

    public CArrayType(CType type, int dimensions) {
        this.type = type.type;
        this.ns = type.ns;
        this.typeNames = type.typeNames;
        setHeader(type.header);
        this.dimensions = dimensions;
    }

    public CType copy() {
        return new CArrayType(this, dimensions);
    }

    String strLevel(int level, boolean ptr) {
        if (level == 0) {
            return ptr ? type : type;
        }
        else if (level == 1) {
            return "array_single<" + strLevel(0, ptr) + ">";
        }
        return "array_multi<" + strLevel(level - 1, ptr) + ">";
    }

    @Override
    public String toString() {
        if (Config.use_vector) {
            return VectorHelper.printLeft(dimensions, this);
        }
        else {
            return strLevel(dimensions, true);
        }
    }
}
