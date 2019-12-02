package com.mesut.j2cpp;

import com.github.javaparser.*;
import java.io.*;
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
            Resolver.srcPath="/home/mesut/Desktop/tmp";
			String a;
			//a="/storage/emulated/0/AppProjects/java2cpp/asd/a.java";
            //a="/storage/extSdCard/asd/dx/dex/src/com/android/dex/Annotation.java";
            //a="/storage/extSdCard/asd/dx/dex/src/com/android/dex/ClassData.java";
			//a="/storage/extSdCard/asd/dx/dex/src/com/android/dex/ClassDef.java";
            a="/storage/extSdCard/asd/dx/dex/src/com/android/dex/Dex.java";
            
            CompilationUnit cu=StaticJavaParser.parse(new File(a));
            
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
            //System.out.println(cu.toStringTree(parser));
            //h.printSource(cs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
