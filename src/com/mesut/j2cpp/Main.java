package com.mesut.j2cpp;

import com.mesut.j2cpp.parser.JavaLexer;
import com.mesut.j2cpp.parser.JavaParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

public class Main {

    public static void main(String[] args) {
        try {
            Resolver.srcPath="/home/mesut/Desktop/tmp";
			String a;
			a="/storage/emulated/0/AppProjects/java2cpp/asd/a.java";
			//a="/home/mesut/Desktop/tmp/a.java";
            JavaLexer lexer= new JavaLexer(CharStreams.fromFileName(a));
            JavaParser parser=new JavaParser(new CommonTokenStream(lexer));

            JavaParser.CompilationUnitContext cu=parser.compilationUnit();

            CUVisitor visitor=new CUVisitor();
            visitor.writer= new PrintWriter(System.out);


            visitor.visit(cu);
            visitor.writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
