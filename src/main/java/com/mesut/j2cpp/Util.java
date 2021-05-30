package com.mesut.j2cpp;

import com.mesut.j2cpp.ast.CClass;
import com.mesut.j2cpp.ast.CHeader;
import com.mesut.j2cpp.ast.CMethod;
import com.mesut.j2cpp.ast.CSource;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class Util {
    static List<String> keywords = new ArrayList<>();

    static {
        try {
            InputStream is = Util.class.getResource("/keywords").openStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String s;
            while ((s = reader.readLine()) != null) {
                keywords.add(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isKeyword(String str) {
        return keywords.contains(str);
    }

    public static void writeHeader(CHeader header, File dir) throws IOException {
        File path = new File(dir, header.path);
        path.getParentFile().mkdirs();
        Files.write(path.toPath(), header.toString().getBytes());
    }

    public static void writeSource(CSource source, File dir) throws IOException {
        File path = new File(dir, source.name);
        path.getParentFile().mkdirs();
        Files.write(path.toPath(), source.toString().getBytes());
    }

    public static String read(File file) throws IOException {
        return read(new FileInputStream(file));
    }

    public static String read(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();

        byte[] buf = new byte[1024];
        int count;

        while ((count = is.read(buf)) != -1) {
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

    public static CMethod getMethod(CClass cc, String name) {
        for (CMethod method : cc.methods) {
            if (method.name.is(name)) return method;
        }
        return null;
    }
}
