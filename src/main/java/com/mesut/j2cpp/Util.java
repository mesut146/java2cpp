package com.mesut.j2cpp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Util<T> {
    public static void save(String data, String file) {
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(data.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        return file.substring(root.length() + 1);
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
}
