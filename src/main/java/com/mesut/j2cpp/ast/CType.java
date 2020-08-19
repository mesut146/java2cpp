package com.mesut.j2cpp.ast;

import com.mesut.j2cpp.Helper;
import com.mesut.j2cpp.cppast.CNode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CType extends CNode {
    public Namespace ns;
    public Namespace scope;
    public String type;
    public int dimensions = 0;//array dims
    public List<CType> typeNames = new ArrayList<>();//generics
    public boolean isTemplate = false;//<T>
    public boolean isPointer = true;

    public CType(String type) {
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

    //print namespace and pointer
    private String withPtr() {
        StringBuilder sb = new StringBuilder(withoutPtr());
        if (isPointer()) {
            sb.append("*");
        }
        return sb.toString();
    }


    private String withoutPtr() {
        return withoutPtr(scope);
    }

    //print namespace and type
    private String withoutPtr(Namespace other) {
        StringBuilder sb = new StringBuilder();
        if (ns != null) {
            if (other == null) {
                sb.append(ns.getAll()).append("::");
            }
            else {
                String norm = ns.normalize(other);
                if (norm != null && !norm.isEmpty()) {
                    sb.append(ns.normalize(other)).append("::");
                }
            }
        }
        sb.append(type);
        return sb.toString();
    }

    public CType copy() {
        CType copied = new CType(type, isTemplate);
        copied.isPointer = isPointer;
        copied.dimensions = dimensions;
        copied.ns = ns;
        copied.typeNames = typeNames;
        return copied;
    }

    public boolean isArray() {
        return dimensions > 0;
    }

    public boolean isPrim() {
        return dimensions == 0 && Helper.isPrim(type);
    }

    public boolean isPointer() {
        return isPointer && !isVoid() && !isPrim() && !isTemplate;
    }

    public boolean isVoid() {
        return type.equals("void");
    }

    @Override
    public String toString() {
        if (isArray()) {
            return strLevel(dimensions, true);
        }
        if (typeNames.size() > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append(withoutPtr());
            sb.append("<");
            sb.append(typeNames.stream().map(CType::withPtr).collect(Collectors.joining(",")));
            sb.append(">");
            if (isPointer()) sb.append("*");
            return sb.toString();
        }
        return withPtr();
    }

    public String normalize(Namespace other) {
        if (isArray()) {
            return strLevel(dimensions, false);
        }
        return withoutPtr(other);
    }

    public String normal() {
        if (isArray()) {
            return strLevel(dimensions, false);
        }
        if (typeNames.size() > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append(withoutPtr());
            sb.append("<");
            sb.append(typeNames.stream().map(CType::withPtr).collect(Collectors.joining(",")));
            sb.append(">");
            return sb.toString();
        }
        return withoutPtr();
    }

    String strLevel(int level, boolean ptr) {
        if (level == 0) {
            return ptr ? withPtr() : withoutPtr();
        }
        else if (level == 1) {
            return "array_single<" + strLevel(level - 1, ptr) + ">";
        }
        return "array_multi<" + strLevel(level - 1, ptr) + ">";
    }

}
