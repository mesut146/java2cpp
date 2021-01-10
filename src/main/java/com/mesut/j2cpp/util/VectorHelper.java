package com.mesut.j2cpp.util;

import com.mesut.j2cpp.ast.CType;
import com.mesut.j2cpp.cppast.CExpression;

import java.util.List;

public class VectorHelper {

    //std::vector<std::vector<std::vector<type*>*>*>*
    public static String printLeft(int dim, CType type) {
        if (dim == 0) {
            return type.normal(type.scope);
        }
        StringBuilder sb = new StringBuilder();
        CType vect = TypeHelper.getVectorType();
        sb.append(vect.normal(type.scope));
        sb.append("<");
        sb.append(printLeft(dim - 1, type));
        sb.append(">*");//pointer
        return sb.toString();
    }

    public static String printRight(List<CExpression> dims, CType type, int i) {
        StringBuilder sb = new StringBuilder();
        CType vect = TypeHelper.getVectorType();

        sb.append(vect.normal(type.scope));
        sb.append("<");
        sb.append(printLeft(dims.size() - i - 1, type));
        sb.append(">");
        sb.append("(");
        sb.append(dims.get(i));
        if (i < dims.size() - 1) {
            sb.append(", new ");
            sb.append(printRight(dims, type, i + 1));
        }
        sb.append(")");
        return sb.toString();
    }
}
