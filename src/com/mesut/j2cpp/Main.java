package com.mesut.j2cpp;

import com.github.javaparser.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.github.javaparser.ast.*;
import com.mesut.j2cpp.visitor.*;
import com.github.javaparser.ast.visitor.*;
import com.github.javaparser.printer.*;

public class Main {


    public static void main(String[] args) {

        try {
            Converter converter;
            String srcPath;
            String destPath;
            boolean android=false;
            if (android){
                srcPath="/storage/extSdCard/asd/dx/dex/src";
                destPath="/storage/emulated/0/AppProjects/java2cpp/asd/test/cpp";
            }else {
                //srcPath="/home/mesut/Desktop/dx-org/src";
                srcPath="/home/mesut/Desktop/src7";
                //srcPath="/home/mesut/IdeaProjects/java2cpp/asd/test/java";
                //destPath="/home/mesut/Desktop/dx-cpp";
                destPath="/home/mesut/Desktop/src7-cpp";
                //destPath="/home/mesut/IdeaProjects/java2cpp/asd/test/cpp";

            }
            converter=new Converter(srcPath,destPath);

			String cls;
			String path;
			//a="/storage/emulated/0/AppProjects/java2cpp/asd/a.java";
            //cls="com/android/dex/Annotation.java";
            //cls="com/android/dex/ClassData.java";
            //cls="com/android/dex/ClassDef.java";
            cls="com/android/dex/Dex.java";
            //cls="MyEnum.java";
            //cls="Generic.java";
            cls="java/lang/Class.java";

            path=srcPath+"/"+cls;
            
            if(args.length>0){
                if(args[0].equals("tree")){
                    CompilationUnit cu=StaticJavaParser.parse(new File(path));
                    String yaml=new YamlPrinter(true).output(cu);
                    File fyml=new File(destPath+"/"+cls.replace(".java",".yaml"));
                    fyml.getParentFile().mkdirs();
                    Files.write(Paths.get(fyml.getAbsolutePath()),yaml.getBytes());
                    System.out.println(yaml);
                    return;
                }
            }
            converter.makeTable();
            converter.convertSingle(cls);
            //converter.convert();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class inner{
        Object field=null;
        void inner_norm(String s){
            System.out.println("inner_norm "+s);
        }

        static void inner_static(String s){
            System.out.println("inner_static "+s);
        }
    }
}
