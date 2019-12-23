package com.mesut.j2cpp;

import com.github.javaparser.JavaParser;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.mesut.j2cpp.visitor.MainVisitor;
import com.mesut.j2cpp.visitor.MethodVisitor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Converter {

    SymbolTable table;
    Resolver resolver;
    String src;
    String dest;
    String sysPath;

    public Converter(String src,String dest) {
        this.src=src;
        this.dest=dest;
    }

    public Resolver getResolver() {
        return resolver;
    }

    public void convert(){
        //makeTable();
        convertDir(new File(src),"");
    }

    public void makeTable() {
        table=new SymbolTable();
        resolver=new Resolver();
        resolver.table=table;
        File dir=new File(src);
        tableDir(dir);
        //System.out.println(table.list.size());
        /*for (Symbol s:table.list) {
            //System.out.println(s.name+" , "+s.pkg);
        }*/
    }

    void tableDir(File dir){
        for (File file:dir.listFiles()){
            if (file.isFile()){
                if (file.getName().endsWith(".java")){
                    try {
                        CompilationUnit cu=StaticJavaParser.parse(file);
                        //add
                        for(TypeDeclaration<?> type:cu.getTypes()){
                            if (type.isClassOrInterfaceDeclaration()){
                                tableClass(type,cu);
                            }
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }else {
                tableDir(file);
            }
        }
    }

    void tableClass(TypeDeclaration<?> type,CompilationUnit cu){
        if (cu.getPackageDeclaration().isPresent()){
            table.addSymbol(cu.getPackageDeclaration().get().getNameAsString(),type.getNameAsString());
        }else {
            table.addSymbol("",type.getNameAsString());
        }
        type.getMembers().forEach(m->{
            if (m.isClassOrInterfaceDeclaration()){
                tableClass(m.asClassOrInterfaceDeclaration(),cu);
            }
        });
    }

    public void convertDir(File dir,String pkg){
        for (File file:dir.listFiles()){
            if (file.isFile()){
                if (file.getName().endsWith(".java")){
                    convertSingle(file,pkg);
                }
            }else {
                convertDir(file,pkg+"/"+file.getName());
            }
        }
    }
    public void convertSingle(File file,String pkg){
        try {
            String path=pkg+"/"+file.getName();

            CompilationUnit cu=StaticJavaParser.parse(file);
            MainVisitor visitor=new MainVisitor();
            visitor.converter=this;

            CHeader header=new CHeader();
            CSource cpp=new CSource();

            header.rpath=path;
            visitor.header=header;
            visitor.mv=new MethodVisitor();
            visitor.mv.converter=this;
            visitor.mv.header=header;
            cpp.h=header;
            cu.accept(visitor,null);

            String hs=header.toString();
            String ss=cpp.toString();
            System.out.println(hs);
            System.out.println("---------------");
            System.out.println(ss);
            File fcpp=new File(dest,path.replace(".java",".cpp"));
            fcpp.getParentFile().mkdirs();
            Files.write(Paths.get(fcpp.getAbsolutePath()),ss.getBytes());

            File fh=new File(dest,path.replace(".java",".h"));
            Files.write(Paths.get(fh.getAbsolutePath()),hs.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void convertSingle(String cls){
        convertSingle(new File(src,cls),"");
    }

}
