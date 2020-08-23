package com.mesut.j2cpp.ast;

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
            return "array_single<" + strLevel(level - 1, ptr) + ">";
        }
        return "array_multi<" + strLevel(level - 1, ptr) + ">";
    }

    @Override
    public String toString() {
        return strLevel(dimensions, true);
    }
}
