package com.mesut.j2cpp.ast;

import com.mesut.j2cpp.Config;
import com.mesut.j2cpp.cppast.CExpression;
import com.mesut.j2cpp.util.PrintHelper;
import com.mesut.j2cpp.util.TypeHelper;
import org.eclipse.jdt.core.dom.ITypeBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CType extends CExpression {
    public Namespace ns;
    public String type;
    public String realName;
    public List<CType> typeNames = new ArrayList<>();//generics
    public boolean isTemplate;//<T>,dynamic type
    public boolean isTypeArg;
    public boolean isPointer;
    public boolean isInner;
    public boolean fromSource;
    public boolean mapped;
    public boolean ref;
    public boolean isRef;
    public ITypeBinding binding;

    public CType() {
    }

    public CType(String type) {
        realName = type.replace("::", ".");//java name
        type = trim(type);
        type = type.replace(".", "::");
        String[] arr = type.split("::");
        this.type = arr[arr.length - 1];
        if (arr.length > 1) {
            ns = new Namespace(type.substring(0, type.lastIndexOf("::")));
        }
    }

    public static CType parse(String name) {
        int pos = name.indexOf("<");
        CType res;
        boolean ref = false;
        if (name.startsWith("&")) {
            ref = true;
            name = name.substring(1);
        }
        if (pos != -1) {
            String real = name.substring(0, pos);
            res = new CType(real);
            int end = name.indexOf(">");
            for (var arg : name.substring(pos + 1, end).split(",")) {
                res.typeNames.add(CType.parse(arg));
            }
        }
        else {
            res = new CType(name);
        }
        res.isRef = ref;
        return res;
    }

    public CType(String type, boolean isTemplate) {
        this(type);
        this.isTemplate = isTemplate;
    }

    public static CType asPtr(CType type) {
        CType p = type.copy();
        p.setPointer(true);
        return p;
    }


    String trim(String str) {
        if (str.contains("<")) {
            return str.substring(0, str.indexOf("<"));
        }
        return str;
    }

    public String getName() {
        return type;
    }

    public CType copy() {
        CType copied = new CType(type, isTemplate);
        copied.isPointer = isPointer;
        copied.ns = ns;
        copied.typeNames = typeNames;
        copied.realName = realName;
        copied.isTypeArg = isTypeArg;
        copied.fromSource = fromSource;
        copied.isInner = isInner;
        copied.ref = ref;
        return copied;
    }

    public boolean isPrim() {
        return TypeHelper.isPrim(type);
    }

    public boolean isPointer() {
        if (isTypeArg) {
            return true;
        }
        return isPointer && !isVoid() && !isPrim() && !isTemplate;
    }

    public void setPointer(boolean pointer) {
        if (!isPrim() && !isTemplate) {
            isPointer = pointer;
        }
    }

    public boolean isVoid() {
        return type.equals("void");
    }

    @Override
    public String toString() {
        if (isPrim() || type.equals("auto") || isTemplate) {
            return type;
        }
        if (Config.normalizeTypes && scope != null) {
            Set<Namespace> usings;
            if (scope instanceof CHeader) {
                usings = ((CHeader) scope).usings;
            }
            else {
                usings = ((CSource) scope).usings;
            }
            return TypeHelper.normalizeType(this, usings).normal(scope);
        }
        return normal(scope);
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
        if (ref) sb.append("&");
        else if (isPointer()) sb.append("*");
        return sb.toString();
    }

    //binary name
    public String basicForm() {
        StringBuilder sb = new StringBuilder();
        if (ns != null && !ns.parts.isEmpty()) {
            sb.append(ns.getAll());
            sb.append("::");
        }
        sb.append(type);
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CType other = (CType) o;
        return realName.equals(other.realName);
    }

    @Override
    public int hashCode() {
        return realName.hashCode();
    }
}
