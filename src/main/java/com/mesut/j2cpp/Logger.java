package com.mesut.j2cpp;

import com.mesut.j2cpp.ast.CClass;
import org.eclipse.jdt.core.dom.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;

public class Logger {
    public static boolean hasErrors = false;
    static PrintWriter writer;

    public static void init(Path file) throws IOException {
        if (!Files.exists(file)) {
            Files.createDirectories(file.getParent());
            Files.createFile(file);
        }
        writer = new PrintWriter(file.toFile());
    }

    public static void log(CClass cc, String msg) {
        log(String.format("'%s' %s", cc.getType(), msg));
        hasErrors = true;
    }
    
    public static void log(ITypeBinding cc, String msg) {
        log(String.format("'%s' %s", cc.getBinaryName(), msg));
        hasErrors = true;
    }
    

    public static void log(String msg) {
        if (writer != null) {
            writer.println(msg);
            writer.flush();
        }
    }

    public static void logBinding(CClass cc, String name) {
        hasErrors = true;
        log(cc, String.format("binding is null for expr '%s'", name));
    }
    public static void logBinding(ITypeBinding cc, String name) {
        hasErrors = true;
        log(cc, String.format("binding is null for expr '%s'", name));
    }
}
