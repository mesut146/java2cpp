package com.mesut.j2cpp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Cmd {
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            usage();
            return;
        }
        String srcDir = null;
        String destDir = null;
        List<String> jars = new ArrayList<>();

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            switch (arg) {
                case "-help":
                case "--help":
                    usage();
                    return;
                case "-src":
                case "-source":
                case "--src":
                case "--source":
                    srcDir = args[++i];
                    break;
                case "-output":
                case "--output":
                    destDir = args[++i];
                    break;
                case "-jar":
                case "--jar":
                    jars.add(args[++i]);
                    break;
            }
            if (srcDir == null) {
                System.out.println("enter source path");
            }
            if (destDir == null) {
                System.out.println("enter output path");
            }
            Converter converter = new Converter(srcDir, destDir);
            converter.jars.addAll(jars);
            converter.makeTable();
            converter.convert();
        }
    }

    static void usage() {
        System.out.println("usage: -src <srcdir> -output <outputdir> -jar <jarpath>\n");
        System.out.println("Options:");
        System.out.println("-src, --src, -source, --source   src directory to be converted");
        System.out.println("-output, --output,               output directory to write c++ .h and .cpp files");
        System.out.println("-jar, --jar                      jar file to add as classpath for symbol resolution, can be multiple");
    }
}
