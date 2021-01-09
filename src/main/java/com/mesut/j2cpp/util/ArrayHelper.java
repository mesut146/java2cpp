package com.mesut.j2cpp.util;

import com.mesut.j2cpp.ast.CType;

public class ArrayHelper {

    public static String print(int level, CType type) {
        if (level == 0) {
            return type.toString() + "*";
        }
        else if (level == 1) {
            return "array_single<" + print(0, type) + ">";
        }
        return "array_multi<" + print(level - 1, type) + ">";
    }
}
