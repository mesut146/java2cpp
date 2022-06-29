package com.mesut.j2cpp.visitor;

import com.mesut.j2cpp.map.Mapper;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Type;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class RustHelper {

    static List<String> keywords = new ArrayList<>();

    static {
        try {
            InputStream is = RustHelper.class.getResource("/rust_keywords").openStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String s;
            while ((s = reader.readLine()) != null) {
                keywords.add(s);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String toRustType(String str) {
        if (str.equals("boolean")) return "bool";
        if (str.equals("byte")) return "i8";
        if (str.equals("short")) return "i16";
        if (str.equals("char")) return "char";
        if (str.equals("int")) return "i32";
        if (str.equals("long")) return "i64";
        if (str.equals("float")) return "f32";
        if (str.equals("double")) return "f64";
        return str;
    }

    public static String mapType(Type type) {
        if (type.isPrimitiveType()) {
            return RustHelper.toRustType(type.toString());
        }
        if (type.isArrayType()) {
            var arr = (ArrayType) type;
            return makeArray(arr.getElementType(), arr.getDimensions());
        }
        return type.toString();
    }

    public static String mapType(ITypeBinding type) {
        if (type.isPrimitive()) {
            return RustHelper.toRustType(type.getName());
        }
        if (type.isArray()) {
            return makeArray(type.getElementType(), type.getDimensions());
        }
        return Mapper.instance.mapType(type).toString();
    }

    public static String makeArray(Type elem, int dims) {
        if (dims == 0) return mapType(elem);
        if (dims == 1) {
            return "Vec<" + mapType(elem) + ">";
        }
        return "Vec<" + makeArray(elem, dims - 1) + ">";
    }

    static String makeArray(ITypeBinding elem, int dims) {
        if (dims == 0) return mapType(elem);
        if (dims == 1) {
            return "Vec<" + mapType(elem) + ">";
        }
        return "Vec<" + makeArray(elem, dims - 1) + ">";
    }

    public static String map(String name) {
        if (keywords.contains(name)) return name + "_renamed";
        return name;
    }

    public static String mapMethodName(IMethodBinding binding) {
        //todo cache
        var clazz = binding.getDeclaringClass();
        if (!clazz.isFromSource()) {
            return binding.getName();
        }
        int cnt = 0;
        for (var method : clazz.getDeclaredMethods()) {
            if (binding.isConstructor() && method.isConstructor()) cnt++;
            else if (method.getName().endsWith(binding.getName())) cnt++;
        }
        if (cnt == 1) {
            return binding.isConstructor() ? "new" : binding.getName();
        }
        //rename by params
        StringBuilder sb = new StringBuilder();
        sb.append(binding.isConstructor() ? "new" : binding.getName());
        for (var param : binding.getParameterTypes()) {
            sb.append("_");
            if (param.isArray()) {
                sb.append(param.getElementType().getName());
            }
            else {
                sb.append(param.getName());
            }
        }
        return sb.toString();
    }
}
