package com.mesut.j2cpp;

import com.mesut.j2cpp.ast.CClass;
import com.mesut.j2cpp.ast.CHeader;
import com.mesut.j2cpp.ast.CMethod;
import com.mesut.j2cpp.ast.CSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isKeyword(String str) {
        return keywords.contains(str);
    }

    public static void writeHeader(CHeader header, Path dir) throws IOException {
        Path path = Paths.get(dir.toString(), header.path);
        Files.createDirectories(path.getParent());
        Files.write(path, header.toString().getBytes());
    }

    public static void writeSource(CSource source, Path dir) throws IOException {
        Path path = Paths.get(dir.toString(), source.name);
        Files.createDirectories(path.getParent());
        Files.write(path, source.toString().getBytes());
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


    public static String trimSuffix(String str, String suffix) {
        if (str.endsWith(suffix)) {
            return str.substring(0, str.length() - suffix.length());
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
