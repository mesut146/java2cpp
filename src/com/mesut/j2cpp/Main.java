package com.mesut.j2cpp;

import com.mesut.j2cpp.parser.*;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import com.mesut.j2cpp.ast.*;


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
            Java8Lexer lexer= new Java8Lexer(CharStreams.fromFileName(a));
            Java8Parser parser=new Java8Parser(new CommonTokenStream(lexer));
            
            Helper.parser=parser;

            Java8Parser.CompilationUnitContext cu=parser.compilationUnit();
            
            CUVisitor visitor=new CUVisitor();
          
            CHeader h=new CHeader();
            CSource cs=new CSource();
            h.rpath="com/asd/test.h";
            cs.h=h;
            visitor.h=h;
            
            visitor.visit(cu);
            
            System.out.println(h);
            System.out.println("----------------");
            System.out.println(cs);
            
            //System.out.println(cu.toStringTree(parser));
            //h.printSource(cs);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
