package com.mesut.j2cpp;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.printer.YamlPrinter;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

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
                cls = "org/jcp/xml/dsig/internal/dom/Utils.java";
                /*srcPath = "/home/mesut/IdeaProjects/java2cpp/asd/test/java";
                destPath = "/home/mesut/IdeaProjects/java2cpp/asd/test/cpp";*/
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

            //cls = "java/lang/Class.java";
            //cls = "com/android/dx/command/Main.java";
            if (args.length > 0 && args[0].equals("tree")) {
                yaml(srcPath, destPath, cls);
                return;
            }
            converter.initParser();
            //converter.initSolver();
            //converter.makeTable();
            converter.convertSingle(cls);
            converter.convert();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    static void yaml(String srcPath, String destPath, String cls) throws IOException {
        String path = srcPath + "/" + cls;
        CompilationUnit cu = StaticJavaParser.parse(new File(path));
        removeComments(cu);
        String yaml = new YamlPrinter(true).output(cu);
        File fyml = new File(destPath + "/" + cls.replace(".java", ".yaml"));
        fyml.getParentFile().mkdirs();
        Files.write(Paths.get(fyml.getAbsolutePath()), yaml.getBytes());
        //System.out.println(yaml);
    }

    static void removeComments(Node node) {
        node.removeComment();
        //node.getChildNodes().forEach(Node::removeComment);
        node.getChildNodes().forEach(Main::removeComments);
    }

    static void resolve() throws FileNotFoundException {
        String dir = "/home/mesut/Desktop/src7/";
        String file = dir + "java/lang/Object.java";

        //TypeSolver typeSolver = new CombinedTypeSolver();
        TypeSolver typeSolver = new JavaParserTypeSolver(dir);

        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(typeSolver);
        StaticJavaParser
                .getConfiguration()
                .setSymbolResolver(symbolSolver);

        CompilationUnit cu = StaticJavaParser.parse(new File(file));

        ClassOrInterfaceDeclaration type = (ClassOrInterfaceDeclaration) cu.getType(0);

        System.out.println(type.resolve().getQualifiedName());

        MethodDeclaration method = type.getMethodsByName("getClass").get(0);
        System.out.println(method.getType().resolve());

        List<ClassOrInterfaceDeclaration> inners = type.getMembers().stream().filter(BodyDeclaration::isClassOrInterfaceDeclaration).map(BodyDeclaration::asClassOrInterfaceDeclaration).collect(Collectors.toList());
        ClassOrInterfaceDeclaration inner = inners.get(0);

        MethodDeclaration m2 = type.getMethodsByName("mymethod").get(0);
        System.out.println(m2.getType().resolve().asReferenceType());
    }
}
