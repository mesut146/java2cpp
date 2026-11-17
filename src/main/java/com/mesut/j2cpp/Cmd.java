package com.mesut.j2cpp;

import com.mesut.j2cpp.map.Mapper;

import java.io.File;
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
        boolean stats = false;
        List<String> cp = new ArrayList<>();
        List<String> mappers = new ArrayList<>();

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            switch (arg) {
                case "-help":
                case "--help":
                    usage();
                    return;
                case "-src":
                case "-source":
                    srcDir = value(args, i, arg);
                    i++;
                    break;
                case "-output":
                case "-out":
                    destDir = value(args, i, arg);
                    i++;
                    break;
                case "-cp":
                case "-classpath":
                    cp.add(value(args, i, arg));
                    i++;
                    break;
                case "-mapper":
                    mappers.add(value(args, i, arg));
                    i++;
                    break;
                case "-stats":
                    stats = true;
                    break;
                case "-no-mappers":
                case "-nomap":
                    Config.useMappers = false;
                    break;
                default:
                    System.err.println("unknown option: " + arg);
                    break;
            }
        }

        if (srcDir == null) {
            System.err.println("missing source path (-src)");
        }
        if (destDir == null) {
            System.err.println("missing output path (-out)");
        }
        if (srcDir == null || destDir == null) {
            usage();
            return;
        }
        if (!new File(srcDir).isDirectory()) {
            System.err.println("source path is not a directory: " + srcDir);
            usage();
            return;
        }
        //resolve after parsing so -mapper can appear before -src
        if (Config.useMappers) {
            for (String path : mappers) {
                Mapper.instance.addMapper(findMapper(path, srcDir).getAbsolutePath());
            }
        }
        else if (!mappers.isEmpty()) {
            System.err.println("warning: -mapper ignored because mappers are disabled (-no-mappers)");
        }

        Converter converter = new Converter(srcDir, destDir);
        if (cp.isEmpty()) {
            System.out.println("converting without classpath");
        }
        else {
            converter.classpath.addAll(cp);
        }
        if (Config.useMappers) {
            Mapper.instance.initMappers();
        }
        if (stats) {
            converter.stats();
        }
        else {
            converter.convert();
        }
    }

    private static String value(String[] args, int i, String option) {
        if (i + 1 >= args.length) {
            throw new IllegalArgumentException("missing value for " + option);
        }
        return args[i + 1];
    }

    private static File findMapper(String path, String srcDir) {
        File file = new File(path);
        if (file.exists()) return file;
        File inSource = new File(srcDir, path);
        if (inSource.exists()) return inSource;
        throw new IllegalArgumentException("mapper not found: " + path);
    }

    static void usage() {
        System.out.println("usage: j2cpp -src <sourcedir> -output <outputdir> -cp <path>\n");
        System.out.println("Options:");
        System.out.println("-source, -src       source directory to be converted");
        System.out.println("-output, -out,      output directory to write c++ .h and .cpp files");
        System.out.println("-cp, --classpath    jar file or source directory for classpath, can be multiple");
        System.out.println("-mapper <path>      additional mapper json, resolved relative to cwd or source dir");
        System.out.println("-stats              collect statistics instead of converting");
        System.out.println("-no-mappers         keep java types and generate library stubs instead");
    }
}
