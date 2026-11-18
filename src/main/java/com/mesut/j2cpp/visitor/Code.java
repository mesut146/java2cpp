package com.mesut.j2cpp.visitor;

import com.mesut.j2cpp.Config;
import com.mesut.j2cpp.Logger;
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
        if (b.getName().equals("void")) return "void";
        if (b.isTypeVariable()) {
            if (!Config.useMappers) {
                return TypeHelper.getObjectType().toString();
            }
            if (b.getDeclaringMethod() == null) {
                return b.getName();
            }
            return TypeVisitor.erasedType(b).toString();
        }

        CType ct = TypeVisitor.fromBinding(b);
        String s = ct.toString();
        for (String u : usings) {
            if (s.startsWith(u + "::")) {
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

    //mark a place where generated C++ intentionally differs from the Java source
    public void note(String message) {
        sb.append("/* j2cpp: ").append(safe(message)).append(" */ ");
        Logger.log(message);
    }

    public void noteLine(String message) {
        sb.append(indent).append("/* j2cpp: ").append(safe(message)).append(" */\n");
        Logger.log(message);
    }

    private static String safe(String message) {
        return message.replace("*/", "* /").replace('\n', ' ');
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
        return type.toString();
    }

    public String mapType(ITypeBinding type) {
        return type.getName();
    }

    public String ptr(ITypeBinding b) {
        String s = str(b);
        //a mapper may turn a wrapper such as Integer into a primitive
        CType ct = TypeVisitor.fromBinding(b);
        if (!b.isPrimitive() && !ct.isPrim() && !ct.isTemplate) {
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