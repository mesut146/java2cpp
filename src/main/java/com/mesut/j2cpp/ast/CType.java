package com.mesut.j2cpp.ast;

import com.mesut.j2cpp.cppast.CExpression;
import com.mesut.j2cpp.util.PrintHelper;
import com.mesut.j2cpp.util.TypeHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CType extends CExpression {
    public Namespace ns;
    public String type;
    public List<CType> typeNames = new ArrayList<>();//generics
    public boolean isTemplate = false;//<T>,dynamic type
    public boolean isPointer = false;

    public CType() {
    }

    public CType(String type) {
        type = type.replace(".", "::");
        String[] arr = type.split("::");
        this.type = arr[arr.length - 1];
        if (arr.length > 1) {
            ns = new Namespace(type.substring(0, type.lastIndexOf("::")));
        }
    }

    public CType(String type, boolean isTemplate) {
        this(type);
        this.isTemplate = isTemplate;
    }

    public String getName() {
        return type;
    }

    public void setPointer(boolean pointer) {
        if (!isPrim() && !isTemplate) {
            isPointer = pointer;
        }
    }

    public CType copy() {
        CType copied = new CType(type, isTemplate);
        copied.isPointer = isPointer;
        copied.ns = ns;
        copied.typeNames = typeNames;
        return copied;
    }

    public boolean isPrim() {
        return TypeHelper.isPrim(type);
    }

    public boolean isPointer() {
        return isPointer && !isVoid() && !isPrim() && !isTemplate;
    }

    public boolean isVoid() {
        return type.equals("void");
    }


    @Override
    public String toString() {
        if (isPrim() || type.equals("auto")) {
            return type;
        }
        if (isTemplate) {
            return type;
        }
        return normal(scope);
        /*if (scope instanceof CHeader) {
            return ((CHeader) scope).normalizeType(this).normal(scope);
        }
        return ((CSource) scope).normalizeType(this).normal(scope);*/
    }

    public String normal(Object scope) {
        StringBuilder sb = new StringBuilder();
        if (ns != null) {
            sb.append(ns.getAll());
            sb.append("::");
        }
        sb.append(type);
        if (typeNames.size() > 0) {
            setScope(scope, typeNames);
            sb.append("<").append(PrintHelper.joinStr(typeNames, ", ")).append(">");
        }
        if (isPointer()) sb.append("*");
        return sb.toString();
    }

    //binary name
    public String basicForm() {
        StringBuilder sb = new StringBuilder();
        if (ns != null) {
            sb.append(ns.getAll());
            sb.append("::");
        }
        sb.append(type);
        return sb.toString();
    }

    public void forward(CHeader header) {
        if (!(this instanceof CUnionType) && !isPrim() && !isVoid() && !isTemplate && !(this instanceof CArrayType)) {//has to be class type
            header.forward(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CType other = (CType) o;
        if (!Objects.equals(ns, other.ns)) {
            return false;
        }
        return Objects.equals(type, other.type);
    }

    @Override
    public int hashCode() {
        int result = ns != null ? ns.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }
}
