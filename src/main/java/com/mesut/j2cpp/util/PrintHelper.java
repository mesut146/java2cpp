package com.mesut.j2cpp.util;

import com.mesut.j2cpp.ast.CType;
import com.mesut.j2cpp.ast.Node;

import java.util.List;

public class PrintHelper {
    public static <T extends Node> void join(Node node, List<T> list, String del) {
        for (int i = 0; i < list.size(); i++) {
            node.append(list.get(i));
            if (i < list.size() - 1) {
                node.append(del);
            }
        }
    }

    public static <T extends Node> void join(Node node, List<T> list, String del, Object scope) {
        for (int i = 0; i < list.size(); i++) {
            list.get(i).scope = scope;
            node.append(list.get(i));
            if (i < list.size() - 1) {
                node.append(del);
            }
        }
    }

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

    public static void join(StringBuilder sb, List<CType> list, String del, Object scope) {
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i).normalized(scope));
            if (i < list.size() - 1) {
                sb.append(del);
            }
        }
    }
}
