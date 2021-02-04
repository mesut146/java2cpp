package com.mesut.j2cpp;

import com.mesut.j2cpp.ast.CHeader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class Util {

    static List<String> c_keywords = Arrays.asList("delete", "virtual", "const");

    public static boolean isKeyword(String str) {
        return c_keywords.contains(str);
    }

    public static void writeHeader(CHeader header, File dir) throws IOException {
        File path = new File(dir, header.rpath);
        path.getParentFile().mkdirs();
        Files.write(path.toPath(), header.toString().getBytes());
    }

    public static String read(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        StringBuilder sb = new StringBuilder();

        byte[] buf = new byte[1024];
        int count;

        while ((count = fis.read(buf)) != -1) {
            sb.append(new String(buf, 0, count));
        }
        return sb.toString();
    }


    public static String relative(String file, String root) {
        if (file.equals(root)) {
            return "";
        }
        if (file.startsWith(root)) {
            return file.substring(root.length() + 1);
        }
        return file;
    }

    public static String relative(File file, File root) {
        return relative(file.getAbsolutePath(), root.getAbsolutePath());
    }

    public static String trimSuffix(String str, String suffix) {
        if (str.endsWith(suffix)) {
            return str.substring(0, str.length() - suffix.length());
        }
        return str;
    }

    public static String trimPrefix(String str, String prefix) {
        if (str.startsWith(prefix)) {
            return str.substring(prefix.length());
        }
        return str;
    }
}
