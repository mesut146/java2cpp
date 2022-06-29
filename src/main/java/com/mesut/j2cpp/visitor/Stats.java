package com.mesut.j2cpp.visitor;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class Stats extends ASTVisitor {
    HashSet<String> types = new HashSet<>();
    //Set<IMethodBinding> methods = new TreeSet<>(get());
    Map<IMethodBinding, Integer> map = new TreeMap<>(get());

    Comparator<IMethodBinding> get() {
        return Comparator.comparing(o -> o.getDeclaringClass().getBinaryName());
    }

    String printType(ITypeBinding type) {
        if (type.isArray()) {
            return printType(type.getElementType()) +
                    "[]".repeat(Math.max(0, type.getDimensions()));
        }
        return type.getErasure().getName();
    }

    public void write(Path destDir) throws IOException {
        Path file = destDir.resolve("stats.txt");
        StringBuilder sb = new StringBuilder();
        var byFreq =
                map.entrySet().stream().sorted(Map.Entry.comparingByValue()).collect(Collectors.toList());
        for (var e : byFreq) {
            var method = e.getKey().getMethodDeclaration();
            String params = Arrays.stream(method.getParameterTypes()).
                    map(this::printType).
                    collect(Collectors.joining(", "));
            var clazz = printType(method.getDeclaringClass());
            sb.append(String.format("%s.%s(%s) %s", clazz, method.getName(), params, map.get(method)));
            sb.append("\n");
        }
//        for (var method : methods) {
//            sb.append(String.format("%s.%s() %s", method.getDeclaringClass().getQualifiedName(), method.getName(), map.get(method)));
//            sb.append("\n");
//        }
        Files.writeString(file, sb);
    }

    @Override
    public boolean visit(MethodInvocation node) {
        if (node.getExpression() != null) {
            var bind = node.resolveMethodBinding();
            if (!bind.getDeclaringClass().isFromSource()) {
                map.put(bind, map.getOrDefault(bind, 0) + 1);
                //methods.add(bind);
            }
        }
        return false;
    }
}
