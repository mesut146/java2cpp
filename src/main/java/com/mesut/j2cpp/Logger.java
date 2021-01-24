package com.mesut.j2cpp;

import com.mesut.j2cpp.ast.CClass;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class Logger {
    static PrintWriter writer;

    public static void init(File file) throws IOException {
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
        writer = new PrintWriter(file);
    }

    public static void log(CClass cc, String msg) {
        if (writer != null)
            writer.println(cc.getType() + ": " + msg);
    }
}
