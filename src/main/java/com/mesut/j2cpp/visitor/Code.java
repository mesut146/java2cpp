package com.mesut.j2cpp.visitor;

import com.mesut.j2cpp.Config;
import com.mesut.j2cpp.ast.CType;
import com.mesut.j2cpp.util.TypeHelper;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Type;

import java.util.ArrayList;
import java.util.List;

public class Code {
    StringBuilder sb = new StringBuilder();
    int level = 0;
    String indent = "";
    List<String> usings = new ArrayList<>();
    boolean rust = false;

    void init() {
        indent = "";
        for (int i = 0; i < level; i++) {
            indent = indent + "    ";
        }
    }

    public void up() {
        level++;
        init();
    }

    public void down() {
        level--;
        init();
    }

    public void clear() {
        sb.setLength(0);
    }

    String str(ITypeBinding b) {
        if (rust) {
            return mapType(b);
        }
        if (b.getName().equals("void")) return "void";
        if (b.isTypeVariable()) {
            return TypeHelper.getObjectType().toString();
        }

        CType ct = TypeVisitor.fromBinding(b);
        if (Config.full) ct.typeNames.clear();
        String s = ct.toString();
        for (String u : usings) {
            if (s.startsWith(u)) {
                return s.substring(u.length() + 2);
            }
        }
        return s;
    }

    public void write(ITypeBinding b) {
        write(str(b));
    }

    String format(String s, Object... args) {
        if (args.length != 0) {
            for (int i = 0; i < args.length; i++) {
                if (args[i] instanceof ITypeBinding type) {
                    args[i] = str(type);
                }
                else if (args[i] instanceof Type type) {
                    args[i] = mapType(type);
                }
            }
            return String.format(s, args);
        }
        return s;
    }

    public void write(String s, Object... args) {
        s = format(s, args);
        sb.append(s);
        if (s.trim().endsWith("{")) up();
    }

    public void line(String s, Object... args) {
        if (s.trim().endsWith("}")) down();
        sb.append(indent);
        write(s, args);
    }

    public String mapType(Type type) {
        if (rust) {
            return RustHelper.mapType(type);
        }
        else {
            return type.toString();
        }
    }

    public String mapType(ITypeBinding type) {
        if (rust) {
            return RustHelper.mapType(type);
        }
        else {
            return type.getName();
        }
    }

    public String ptr(ITypeBinding b) {
        String s = str(b);
        if (!b.isPrimitive()) {
            s = s + "*";
        }
        return s;
    }

    public String ptr(CType b) {
        String s = b.toString();
        s = s + "*";
        return s;
    }

    public String toString() {
        return sb.toString();
    }

}