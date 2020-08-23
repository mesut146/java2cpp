package com.mesut.j2cpp.ast;

import com.mesut.j2cpp.cppast.CExpression;
import com.mesut.j2cpp.util.Helper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CType extends CExpression {
    public Namespace ns;
    public CHeader header;
    public CSource source;
    public String type;
    public List<CType> typeNames = new ArrayList<>();//generics
    public boolean isTemplate = false;//<T>
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

    public CType(String type, CHeader header) {
        this(type);
        setHeader(header);
    }

    public CType(String type, boolean isTemplate) {
        this(type);
        this.isTemplate = isTemplate;
    }

    public String getName() {
        return type;
    }


    public CType copy() {
        CType copied = new CType(type, isTemplate);
        copied.isPointer = isPointer;
        copied.ns = ns;
        copied.typeNames = typeNames;
        copied.setHeader(header);
        return copied;
    }


    public boolean isPrim() {
        return Helper.isPrim(type);
    }

    public boolean isPointer() {
        return isPointer && !isVoid() && !isPrim() && !isTemplate;
    }

    public boolean isVoid() {
        return type.equals("void");
    }

    @Override
    public String toString() {
        return normalized();
        //return normal();
    }

    public String normalized() {
        if (isPrim()) {
            return type;
        }
        if (isTemplate) {
            return type;
        }
        return header.normalizeType(this).normal();
    }

    public String normal() {
        StringBuilder sb = new StringBuilder();
        if (ns != null) {
            sb.append(ns.getAll());
            sb.append("::");
        }
        sb.append(type);
        if (typeNames.size() > 0) {
            sb.append("<");
            sb.append(typeNames.stream().map(CType::toString).collect(Collectors.joining(",")));
            sb.append(">");
        }
        if (isPointer()) sb.append("*");
        return sb.toString();
    }

    //print namespace and pointer
    String withPtr() {
        StringBuilder sb = new StringBuilder(withoutPtr());
        if (isPointer()) {
            sb.append("*");
        }
        return sb.toString();
    }


    String withoutPtr() {
        return type;
    }

    public CType setHeader(CHeader header) {
        if (header != null) {
            this.header = header;
            this.source = header.source;
        }
        return this;
    }

    public CType setHeader(CSource source) {
        return setHeader(source.header);
    }
}
