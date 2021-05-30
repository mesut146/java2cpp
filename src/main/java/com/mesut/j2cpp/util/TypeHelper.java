package com.mesut.j2cpp.util;

import com.mesut.j2cpp.Config;
import com.mesut.j2cpp.ast.CType;
import com.mesut.j2cpp.ast.Namespace;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TypeHelper {

    static HashMap<String, String> prims = new HashMap<>();

    static {
        prims.put("byte", Config.type_byte);
        prims.put("char", Config.type_char);
        prims.put("short", Config.type_short);
        prims.put("float", Config.type_float);
        prims.put("int", Config.type_int);
        prims.put("double", Config.type_double);
        prims.put("long", Config.type_long);
        prims.put("boolean", Config.type_boolean);
    }

    public static boolean isPrim(String ty) {
        return prims.containsValue(ty);
    }

    public static String toCType(String ty) {
        if (prims.containsKey(ty)) {
            return prims.get(ty);
        }
        return ty;
    }

    public static CType getStringType() {
        return new CType("java.lang.String");
    }

    public static CType getObjectType() {
        return new CType("java.lang.Object");
    }

    public static CType getClassType() {
        return new CType("java.lang.Class");
    }

    public static CType getVectorType() {
        return new CType("std::vector");
    }

    public static CType getEnumType() {
        return new CType("java.lang.Enum");
    }

    public static CType getAnnotationType() {
        return new CType("java.lang.Annotation");
    }

    public static CType getVoidType() {
        return new CType("void");
    }

    public static CType getAutoType() {
        return new CType("auto");
    }


    //trim type's namespace by usings
    //java::lang::String   using java::lang -> String
    public static CType normalizeType(CType type, Set<Namespace> usings) {
        if (type.ns == null) {
            return type;
        }
        CType copied = type.copy();
        Namespace typeNs = copied.ns;
        //trim matching using's ns
        for (Namespace ns : usings) {
            if (ns.getAll().equals(typeNs.getAll())) {
                copied.ns = null;
                return copied;
            }
            else {
                return copied;
            }
            /*if (ns.getAll().startsWith(typeNs.getAll())) {
                String str = ns.getAll().substring(typeNs.getAll().length());
                if (str.startsWith("::")) {
                    str = str.substring(2);
                }
                if (str.isEmpty()) {
                    copied.ns = null;
                }
                else {
                    copied.ns = new Namespace(str);
                }
                return copied;
            }*/
        }
        return copied;
    }

    public static boolean canCast(Expression e, ITypeBinding expr, ITypeBinding type) {
        if (expr.equals(type)) return true;
        if (type.isPrimitive()) {
            return expr.isPrimitive() || e.resolveBoxing() || e.resolveUnboxing();
        }
        HashSet<ITypeBinding> set = new HashSet<>();
        collectBases(expr, set);
        return set.contains(type);
    }

    public static void collectBases(ITypeBinding type, Set<ITypeBinding> set) {
        if (set.contains(type)) return;
        set.add(type);
        if (type.getSuperclass() != null) {
            if (!type.getSuperclass().getQualifiedName().equals("java.lang.Object") || Config.baseClassObject) {
                collectBases(type.getSuperclass(), set);
            }
        }
        for (ITypeBinding iface : type.getInterfaces()) {
            collectBases(iface, set);
        }

    }

}
