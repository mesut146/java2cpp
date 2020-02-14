package com.mesut.j2cpp;

import java.io.File;
import java.io.FileOutputStream;

public class Util<T> {
    public static void save(String data, String file) {
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(data.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String relative(String file, String root) {
        return file.substring(root.length()+1);
    }

    public static String relative(File file, File root) {
        return relative(file.getAbsolutePath(), root.getAbsolutePath());
    }
}
