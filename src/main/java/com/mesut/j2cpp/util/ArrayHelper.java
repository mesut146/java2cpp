package com.mesut.j2cpp.util;

import com.mesut.j2cpp.ast.CType;
import com.mesut.j2cpp.cppast.CExpression;
import com.mesut.j2cpp.cppast.expr.CObjectCreation;

import java.util.List;

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

    public static CExpression makeRight(CType elemType, List<CExpression> dims) {
        return makeRightAlloc(elemType, dims.size(), dims);
    }

    public static CExpression makeRightAlloc(CType elemType, int size, List<CExpression> dims) {
        CObjectCreation cur = new CObjectCreation();
        cur.type = makeArrayType(elemType, size);
        cur.args.add(dims.get(0));
        if (size > 1) {//todo zero
            cur.args.add(makeRightAlloc(elemType, size - 1, dims.subList(1, dims.size())));//allocator
        }
        return cur;
    }
}
