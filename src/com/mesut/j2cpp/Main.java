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
            String destPath;
            boolean android=false;
            if (android){
                Resolver.srcPath="/storage/extSdCard/asd/dx/dex/src";
                destPath="/storage/emulated/0/AppProjects/java2cpp/asd/test/cpp";
            }else {
                Resolver.srcPath="/home/mesut/Desktop/dx-org/src";
                destPath="/home/mesut/Desktop/dx-cpp";

            }
            //Resolver.srcPath="/storage/emulated/0/AppProjects/java2cpp/asd/test/java";

			String cls;
			String path;
			//a="/storage/emulated/0/AppProjects/java2cpp/asd/a.java";
            //cls="com/android/dex/Annotation.java";
            //cls="com/android/dex/ClassData.java";
            //cls="com/android/dex/ClassDef.java";
            cls="com/android/dex/Dex.java";
            //cls="MyEnum.java";
            
            path=Resolver.srcPath+"/"+cls;

            CompilationUnit cu=StaticJavaParser.parse(new File(path));
            
            if(args.length>0){
                if(args[0].equals("tree")){
                    String yaml=new YamlPrinter(true).output(cu);
                    File fyml=new File(destPath+"/"+cls.replace(".java",".yaml"));
                    fyml.getParentFile().mkdirs();
                    Files.write(Paths.get(fyml.getAbsolutePath()),yaml.getBytes());
                    System.out.println(yaml);
                    return;
                }
            }
            
            MainVisitor visitor=new MainVisitor();
            CHeader h=new CHeader();
            CSource cpp=new CSource();
            h.rpath="test.h";
            visitor.h=h;
            cpp.h=h;
            cu.accept(visitor,null);
            
            String hs=h.toString();
            String ss=cpp.toString();
            System.out.println(hs);
            System.out.println("---------------");
            System.out.println(ss);
            File fcpp=new File(destPath+"/"+cls.replace(".java",".cpp"));
            fcpp.getParentFile().mkdirs();
            Files.write(Paths.get(fcpp.getAbsolutePath()),ss.getBytes());
            
            File fh=new File(destPath+"/"+cls.replace(".java",".h"));
            Files.write(Paths.get(fh.getAbsolutePath()),hs.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    

}
