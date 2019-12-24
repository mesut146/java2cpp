package com.mesut.j2cpp;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.mesut.j2cpp.ast.CHeader;
import com.mesut.j2cpp.ast.CSource;
import com.mesut.j2cpp.visitor.MainVisitor;
import com.mesut.j2cpp.visitor.MethodVisitor;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Converter {

    SymbolTable table;
    Resolver resolver;
    String src;
    String dest;
    String sysPath;
    List<Holder> units;

    class Holder{
        CompilationUnit cu;
        String name;//asd.java

        public Holder(CompilationUnit cu, String name) {
            this.cu = cu;
            this.name = name;
        }
    }

    public Converter(String src, String dest) {
        this.src = src;
        this.dest = dest;
    }

    public Resolver getResolver() {
        return resolver;
    }

    public void convert() {
        makeTable();
        //convertDir(new File(src), "");
        for (Holder h:units){
            String pkg="";
            if (h.cu.getPackageDeclaration().isPresent()){
                pkg=h.cu.getPackageDeclaration().get().getNameAsString();
            }
            convertSingle(pkg.replaceAll("\\.","/")+"/"+h.name,h.cu);
        }
    }

    public void makeTable() {
        table = new SymbolTable();
        resolver = new Resolver(table);
        File dir = new File(src);
        units=new ArrayList<>();
        //tableDir(dir);
        //System.out.println(table.list.size());
        /*for (Symbol s:table.list) {
            //System.out.println(s.name+" , "+s.pkg);
        }*/
    }

    void tableDir(File dir) {
        for (File file : dir.listFiles()) {
            if (file.isFile()) {
                if (file.getName().endsWith(".java")) {
                    try {
                        CompilationUnit cu = StaticJavaParser.parse(file);
                        units.add(new Holder(cu,file.getName()));
                        // cu,pkg,name
                        for (TypeDeclaration<?> type : cu.getTypes()) {
                            if (type.isClassOrInterfaceDeclaration()) {
                                tableClass(type, cu);
                            }
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                tableDir(file);
            }
        }
    }

    void tableClass(TypeDeclaration<?> type, CompilationUnit cu) {
        if (cu.getPackageDeclaration().isPresent()) {
            table.addSymbol(cu.getPackageDeclaration().get().getNameAsString(), type.getNameAsString());
        } else {
            table.addSymbol("", type.getNameAsString());
        }
        type.getMembers().forEach(m -> {
            if (m.isClassOrInterfaceDeclaration()) {
                tableClass(m.asClassOrInterfaceDeclaration(), cu);
            }
        });
    }

    /*public void convertDir(File dir, String pkg) throws FileNotFoundException {
        for (File file : dir.listFiles()) {
            if (file.isFile()) {
                if (file.getName().endsWith(".java")) {
                    convertSingle(file, pkg,StaticJavaParser.parse(file));
                }
            } else {
                convertDir(file, pkg + "/" + file.getName());
            }
        }
    }*/

    public void convertSingle(String path,CompilationUnit cu) {
        try {

            CHeader header = new CHeader();
            CSource cpp = new CSource(header);
            header.rpath = path.replace(".java", ".h");
            header.importStar.add("java/lang");

            MainVisitor visitor = new MainVisitor(this, header);

            //CompilationUnit cu = StaticJavaParser.parse(file);
            cu.accept(visitor, null);

            String hs = header.toString();
            String ss = cpp.toString();
            System.out.println(hs);
            System.out.println("---------------");
            System.out.println(ss);
            File fcpp = new File(dest, path.replace(".java", ".cpp"));
            fcpp.getParentFile().mkdirs();
            Files.write(Paths.get(fcpp.getAbsolutePath()), ss.getBytes());

            File fh = new File(dest, path.replace(".java", ".h"));
            Files.write(Paths.get(fh.getAbsolutePath()), hs.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void convertSingle(String cls) throws FileNotFoundException {
        File file=new File(src,cls);
        convertSingle(cls, StaticJavaParser.parse(file));
    }

}
