package com.mesut.j2cpp;

public class Main {


    public static void main(String[] args) {

        try {
            /*if (true) {
                resolve();
                return;
            }*/
            Converter converter;
            String srcPath;
            String destPath;
            String cls = "";
            String rt = "";
            boolean android = false;
            if (android) {
                rt = "/storage/extSdCard/lib/rt7.jar";
                srcPath = "/storage/extSdCard/asd/dx/dex/src";
                destPath = "/storage/emulated/0/AppProjects/java2cpp/asd/test/cpp";
                //cls = "test.java";
            }
            else {
                rt = "/home/mesut/Desktop/rt7.jar";
                //srcPath = "/home/mesut/Desktop/dx-org";
                //destPath = "/home/mesut/Desktop/dx-cpp";
                srcPath = "/home/mesut/Desktop/src7";
                destPath = "/home/mesut/Desktop/src7-cpp";
                //cls = "org/jcp/xml/dsig/internal/dom/Utils.java";
                //srcPath = "/home/mesut/IdeaProjects/java2cpp/asd/test/java";
                //destPath = "/home/mesut/IdeaProjects/java2cpp/asd/test/cpp";
                //cls = "base/a.java";
                //cls = "base/Generic.java";
                //cls="base/iface.java";
                //cls = "base/TryTest.java";
            }
            converter = new Converter(srcPath, destPath);
            converter.addClasspath(rt);
            converter.addClasspath(srcPath);
            //converter.addClasspath("/home/mesut/Desktop/src7");
            //converter.addIncludeDir("java/lang");
            //converter.addInclude("java/util");
            //converter.addInclude("java/io");
            //converter.addInclude("java/nio");

            cls = "java/lang/Class.java";
            //cls = "com/android/dx/command/Main.java";
            //cls = "com/android/dex/MethodHandle.java";
            //cls = "com/android/multidex/FolderPathElement.java";
            //cls = "com/android/multidex/ClassPathElement.java";
            if (args.length > 0 && args[0].equals("tree")) {
                //yaml(srcPath, destPath, cls);
                return;
            }
            converter.initParser();
            converter.setDebugAll(false);
            converter.setDebugSource(true);

            converter.convertSingle(cls);
            //converter.convert();


        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
