package com.mesut.j2cpp.util;

import com.mesut.j2cpp.ast.Namespace;
import com.mesut.j2cpp.ast.Node;
import com.mesut.j2cpp.cppast.stmt.CBlockStatement;

import java.util.List;

public class PrintHelper {
    public static <T extends Node> String join(List<T> list, String del, Object scope) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            list.get(i).scope = scope;
            sb.append(list.get(i));
            if (i < list.size() - 1) {
                sb.append(del);
            }
        }
        return sb.toString();
    }

    public static <T extends Node> void join(StringBuilder sb, List<T> list, String del) {
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i));
            if (i < list.size() - 1) {
                sb.append(del);
            }
        }
    }

    public static <T extends Node> void join(StringBuilder sb, List<T> list, String del, Object scope) {
        for (int i = 0; i < list.size(); i++) {
            list.get(i).scope = scope;
            sb.append(list.get(i));
            if (i < list.size() - 1) {
                sb.append(del);
            }
        }
    }

    public static String strBody(Node body) {
        if (body instanceof CBlockStatement) {
            return body.toString();
        }
        else {
            return "\n    " + body;
        }
    }

    public static String join(List<String> list, String del) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i));
            if (i < list.size() - 1) {
                sb.append(del);
            }
        }
        return sb.toString();
    }

    public static String using(Namespace ns) {
        return String.format("using namespace %s;", ns.getAll());
    }

    public static String include(String str) {
        return String.format("#include \"%s\"", str);
    }

    public static String body(String body, String indent) {
        StringBuilder sb = new StringBuilder();
        for (String line : body.split("\n")) {
            sb.append(indent).append(line).append("\n");
        }
        return sb.toString();
    }

}
