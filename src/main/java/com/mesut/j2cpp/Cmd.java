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
        List<String> cp = new ArrayList<>();

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            switch (arg) {
                case "-help":
                case "--help":
                    usage();
                    return;
                case "-src":
                case "-source":
                    srcDir = args[++i];
                    break;
                case "-output":
                case "-out":
                    destDir = args[++i];
                    break;
                case "-cp":
                case "-classpath":
                    cp.add(args[++i]);
                    break;
            }
            if (srcDir == null) {
                System.out.println("enter source path");
            }
            if (destDir == null) {
                System.out.println("enter output path");
            }
            Converter converter = new Converter(srcDir, destDir);
            if (cp.isEmpty()) {
                System.out.println("converting without classpath");
            } else {
                converter.classpath.addAll(cp);
            }
            converter.makeTable();
            converter.convert();
        }
    }

    static void usage() {
        System.out.println("usage: j2cpp -src <sourcedir> -output <outputdir> -cp <path>\n");
        System.out.println("Options:");
        System.out.println("-source, -src       source directory to be converted");
        System.out.println("-output, -out,      output directory to write c++ .h and .cpp files");
        System.out.println("-cp, --classpath    jar or source directory for classpath, can be multiple");
    }
}
