package com.mesut.j2cpp;

import java.io.FileOutputStream;

public class Util {
    public static void save(String data, String file) {
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(data.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
