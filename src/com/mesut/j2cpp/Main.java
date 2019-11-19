package com.mesut.j2cpp;

import com.mesut.j2cpp.parser.JavaLexer;
import com.mesut.j2cpp.parser.JavaParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;


//in a method a param must be pointer but reassignmet cwnt change its org value
//if a param assign happens turn it into local var
//nope,just pointer is fine

public class Main {

    public static void main(String[] args) {
        try {
            Resolver.srcPath="/home/mesut/Desktop/tmp";
			String a;
			//a="/storage/emulated/0/AppProjects/java2cpp/asd/a.java";
            a="/storage/extSdCard/asd/dx/dex/src/com/android/dex/Annotation.java";
			
            JavaLexer lexer= new JavaLexer(CharStreams.fromFileName(a));
            JavaParser parser=new JavaParser(new CommonTokenStream(lexer));

            JavaParser.CompilationUnitContext cu=parser.compilationUnit();
   
            CUVisitor visitor=new CUVisitor();
          
            CHeader h=new CHeader();
            CSource cs=new CSource();
            h.rpath="com/asd/test.h";
            visitor.h=h;
            
            visitor.visit(cu);
            h.print();
            
            System.out.println(h.toString());
            //h.printSource(cs);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
