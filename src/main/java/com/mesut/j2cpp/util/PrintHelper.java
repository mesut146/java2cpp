package com.mesut.j2cpp.util;

import com.mesut.j2cpp.ast.Namespace;
import com.mesut.j2cpp.ast.Node;
import com.mesut.j2cpp.cppast.stmt.CBlockStatement;

import java.util.List;

public class PrintHelper {

    public static <T> String joinStr(List<T> list, String del) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i));
            if (i < list.size() - 1) {
                sb.append(del);
            }
        }
        return sb.toString();
    }

    public static String strBody(Node body) {
        if (body instanceof CBlockStatement) {
            return body.toString();
        }
        else {
            return "\n    " + body;
        }
    }

    public static String using(Namespace ns) {
        return String.format("using namespace %s;", ns.getAll());
    }

    public static String body(String body, String indent) {
        StringBuilder sb = new StringBuilder();
        for (String line : body.split("\n")) {
            sb.append(indent).append(line).append("\n");
        }
        return sb.toString();
    }

    public static String body(String body, String indent, boolean first) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (String line : body.split("\n")) {
            if (i > 0 || !first) {
                sb.append(indent);
            }
            sb.append(line).append("\n");
            i++;
        }
        return sb.toString();
    }

}
