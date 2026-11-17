package com.mesut.j2cpp.util;

import com.mesut.j2cpp.ast.CType;

public class ArrayHelper {

    public static CType makeArrayType(CType elemType, int size) {
        elemType.setPointer(true);
        CType vect = TypeHelper.getVectorType();
        CType last = vect;
        for (int i = 0; i < size - 1; i++) {
            CType tmp = TypeHelper.getVectorType();
            tmp.setPointer(true);
            last.typeNames.add(tmp);
            last = tmp;
        }
        last.typeNames.add(elemType);
        return vect;
    }
}
