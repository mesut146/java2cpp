package com.mesut.j2cpp;

import com.mesut.j2cpp.ast.CClass;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class Logger {
    public static boolean hasErrors = false;
    static PrintWriter writer;

    public static void init(File file) throws IOException {
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
        writer = new PrintWriter(file);
    }

    public static void log(CClass cc, String msg) {
        log(cc.getType() + ": " + msg);
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
        log(cc, "binding is null for " + name + " conversion may have problems");
    }
}
