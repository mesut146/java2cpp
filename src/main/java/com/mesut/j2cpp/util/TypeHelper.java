package com.mesut.j2cpp.util;

import com.mesut.j2cpp.Config;
import com.mesut.j2cpp.ast.CType;
import com.mesut.j2cpp.ast.Namespace;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TypeHelper {

    //JDT often exposes legacy JDK APIs as raw generic types; the generated
    //stubs are templates, so a raw use still needs Object type arguments
    private static final Map<String, Integer> GENERIC_ARITIES = new HashMap<>();

    static {
        String[] one = {
                "java.lang.Class", "java.lang.Comparable", "java.lang.Iterable",
                "java.lang.constant.DynamicConstantDesc",
                "java.util.Iterator", "java.util.Collection", "java.util.List",
                "java.util.Set", "java.util.Queue", "java.util.Deque", "java.util.Optional",
                "java.util.function.Supplier", "java.util.function.Consumer",
                "java.util.function.Predicate", "java.util.function.Function",
                "java.util.function.UnaryOperator", "java.util.stream.Stream"
        };
        for (String name : one) {
            GENERIC_ARITIES.put(name, 1);
        }
        GENERIC_ARITIES.put("java.util.Map", 2);
        GENERIC_ARITIES.put("java.util.Map.Entry", 2);
        GENERIC_ARITIES.put("java.util.function.BiFunction", 3);
        GENERIC_ARITIES.put("java.util.function.BiConsumer", 2);
        GENERIC_ARITIES.put("java.util.function.BiPredicate", 2);
        GENERIC_ARITIES.put("java.util.stream.Collector", 3);
    }

    public static int genericArity(ITypeBinding binding) {
        if (binding == null) return 0;
        ITypeBinding erasure = binding.getErasure();
        String name = erasure.getQualifiedName();
        if (name == null) return 0;
        Integer arity = GENERIC_ARITIES.get(name);
        return arity == null ? 0 : arity;
    }

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

    //method type parameters that do not shadow the enclosing class's parameters
    public static List<ITypeBinding> methodTypeParams(IMethodBinding method, ITypeBinding declaringClass) {
        List<String> classNames = new ArrayList<>();
        if (declaringClass != null) {
            for (ITypeBinding p : declaringClass.getTypeParameters()) {
                classNames.add(p.getName());
            }
        }
        List<ITypeBinding> result = new ArrayList<>();
        for (ITypeBinding p : method.getTypeParameters()) {
            if (!classNames.contains(p.getName())) {
                result.add(p);
            }
        }
        return result;
    }

    public static String templateLineForNames(List<String> names) {
        if (names.isEmpty()) return null;
        StringBuilder sb = new StringBuilder("template <");
        for (int i = 0; i < names.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append("class ").append(names.get(i));
        }
        return sb.append(">").toString();
    }

    public static String templateLine(List<ITypeBinding> params) {
        if (params.isEmpty()) return null;
        StringBuilder sb = new StringBuilder("template <");
        for (int i = 0; i < params.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append("class ").append(params.get(i).getName());
        }
        return sb.append(">").toString();
    }

    //describe how Java type parameters were erased in the generated signature
    public static String erasureNote(IMethodBinding method) {
        ITypeBinding[] vars = method.getTypeParameters();
        if (vars.length == 0) return null;
        StringBuilder sb = new StringBuilder("method type parameters erased: ");
        for (int i = 0; i < vars.length; i++) {
            if (i > 0) sb.append(", ");
            ITypeBinding erased = vars[i].getErasure();
            sb.append("<").append(vars[i].getName()).append("> -> ")
                    .append(erased == null ? "java.lang.Object" : erased.getQualifiedName());
        }
        return sb.toString();
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
        }
        return copied;
    }

    public static boolean canCast(Expression e, ITypeBinding expr, ITypeBinding type) {
        if (expr == null) return true;
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
