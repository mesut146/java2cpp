package com.mesut.j2cpp;

import com.github.javaparser.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.github.javaparser.ast.*;
import com.mesut.j2cpp.visitor.*;
import com.github.javaparser.ast.visitor.*;
import com.github.javaparser.printer.*;

//in a method a param must be pointer but reassignmet cwnt change its org value
//if a param assign happens turn it into local var
//nope,just pointer is fine

public class Main {

    public static void main(String[] args) {
        try {
            //Resolver.srcPath="/home/mesut/Desktop/dx-org/src";
            Resolver.srcPath="/storage/emulated/0/AppProjects/java2cpp/asd/test/java";
            //String destPath="/home/mesut/Desktop/dx-cpp";
            String destPath="/storage/emulated/0/AppProjects/java2cpp/asd/test/cpp";
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
                    YamlPrinter.print(cu);
                    return;
                }
            }
            
            MainVisitor vi=new MainVisitor();
            CHeader h=new CHeader();
            CSource cpp=new CSource();
            h.rpath="test.h";
            vi.h=h;
            cu.accept(vi,null);
            //YamlPrinter.print(cu);
            System.out.println(vi.h);
            System.out.println("---------------");
            
            cpp.h=vi.h;
            System.out.println(cpp);
            File fcpp=new File(destPath+"/"+cls.replace(".java",".cpp"));
            fcpp.getParentFile().mkdirs();
            Files.write(Paths.get(fcpp.getAbsolutePath()),cpp.toString().getBytes());
            
            File fh=new File(destPath+"/"+cls.replace(".java",".h"));
            Files.write(Paths.get(fh.getAbsolutePath()),h.toString().getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
