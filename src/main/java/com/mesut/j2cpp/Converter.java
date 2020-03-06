package com.mesut.j2cpp;


import com.mesut.j2cpp.ast.CHeader;
import com.mesut.j2cpp.ast.CSource;
import com.mesut.j2cpp.visitor.MainVisitor;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Converter {

    SymbolTable table;
    Resolver resolver;
    //JavaParser javaParser;
    String srcDir;//source folder
    String destDir;//destinaytion folder for c++ files
    String sysPath;//openjdk sources
    List<UnitMap> units;//parsed sources
    boolean includeAll = false;
    List<PackageName> includeDirs = new ArrayList<>();
    List<String> excludeDirs = new ArrayList<>();
    List<String> includeClasses = new ArrayList<>();
    List<String> excludeClasses = new ArrayList<>();
    //look fist this while resolving
    List<PackageNode> packageHierarchy = new ArrayList<>();
    public List<String> classpath = new ArrayList<>();
    //public SymbolResolver symbolResolver;
    public CMakeWriter cMakeWriter;
    public CMakeWriter.Target target;
    public boolean debug_output = false;
    ASTParser parser;

    public Converter(String srcDir, String destDir) throws IOException {
        this.srcDir = srcDir;
        this.destDir = destDir;
        cMakeWriter = new CMakeWriter("myproject");
        target = cMakeWriter.addTarget("mylib", false);
    }

    public void addIncludeDir(String prefix) {
        includeDirs.add(new PackageName(prefix));
    }

    public void addExcludeDir(String prefix) {
        excludeDirs.add(prefix);
    }

    public void addIncludeClass(String name) {
        includeClasses.add(name);
    }

    public void addExcludeClass(String name) {
        excludeClasses.add(name);
    }

    public void setIncludeAll(boolean flag) {
        this.includeAll = flag;
    }

    public Resolver getResolver() {
        return resolver;
    }

    public void fixImports() {

    }


    public void addClasspath(String dir) {
        classpath.add(dir);
    }

    public void initParser() {
        parser = ASTParser.newParser(AST.JLS13);
        List<String> cpDirs = new ArrayList<>();
        List<String> cpJars = new ArrayList<>();

        for (String path : classpath) {
            if (path.endsWith(".jar")) {
                cpJars.add(path);
            }
            else {
                cpDirs.add(path);
            }
        }
        parser.setEnvironment(cpJars.toArray(new String[0]), cpDirs.toArray(new String[0]), null, false);


        parser.setResolveBindings(true);
        parser.setBindingsRecovery(true);
        parser.setStatementsRecovery(true);
    }

    public void convert() {
        try {
            convertDir(new File(srcDir), "");
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*for (UnitMap h : units) {
            String pkg = "";
            if (h.cu.getPackageDeclaration().isPresent()) {
                pkg = h.cu.getPackageDeclaration().get().getNameAsString();
            }
            convertSingle(pkg.replaceAll("\\.", "/") + "/" + h.name, h.cu);
        }
        File cmakeFile = new File(destDir, "CMakeLists.txt");
        try {
            Files.write(cmakeFile.toPath(), cMakeWriter.generate().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        System.out.println("conversion done");
    }

    void convertDir(File dir, String str) throws IOException {
        for (File file : Objects.requireNonNull(dir.listFiles())) {
            if (file.isDirectory()) {
                convertDir(file, str);
            }
            else if (file.getName().endsWith(".java")) {
                parser.setSource(Util.read(file).toCharArray());
                parser.setUnitName(file.getPath());
                CompilationUnit unit = (CompilationUnit) parser.createAST(null);

                //CompilationUnit cu = javaParser.parse(file).getResult().get();
                convertSingle(Util.relative(file.getAbsolutePath(), srcDir), unit);
            }
        }
    }

    /*void initSolver() throws IOException {
        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        JavaParserTypeSolver dirSolver = new JavaParserTypeSolver(srcDir);

        combinedTypeSolver.add(dirSolver);//current dir
        for (String cp : classpath) {
            if (cp.endsWith(".jar")) {
                JarTypeSolver jarTypeSolver = new JarTypeSolver(cp);
                combinedTypeSolver.add(jarTypeSolver);
            } else {//directory
                JavaParserTypeSolver cpSolver = new JavaParserTypeSolver(cp);
                combinedTypeSolver.add(cpSolver);
            }
        }
        symbolResolver = new JavaSymbolSolver(combinedTypeSolver);
        javaParser = new JavaParser(new ParserConfiguration().setSymbolResolver(symbolResolver));
    }*/


    /*public void makeTable() throws IOException {
        table = new SymbolTable();
        resolver = new Resolver(table);
        File dir = new File(srcDir);
        units = new ArrayList<>();

        tableDir(dir, null);
        System.out.println("total " + units.size() + " classes");
        for (PackageNode node : packageHierarchy) {
            System.out.println(node);
        }
        /*for (Symbol s:table.list) {
            System.out.println(s.name+" , "+s.pkg);
        }
    }*/

    //walk in source directory,parse all files and add classes to symbol table
    //useful for converting directory
    /*void tableDir(File dir, PackageNode node) {
        System.out.println("entering dir=" + dir);

        for (File file : dir.listFiles()) {
            if (file.isFile()) {
                if (file.getName().endsWith(".java")) {
                    try {
                        CompilationUnit cu = javaParser.parse(file).getResult().get();
                        units.add(new UnitMap(cu, file.getName()));
                        // cu,pkg,name
                        for (TypeDeclaration<?> type : cu.getTypes()) {
                            if (type.isClassOrInterfaceDeclaration()) {
                                tableClass(type, cu);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            else {
            //dir
                PackageNode sub;
                if (node == null) {
                    sub = new PackageNode(file.getName());
                    packageHierarchy.add(sub);
                } else {
                    sub = node.addSub(file.getName());
                }
                if (includeDirs.isEmpty()) {
                    tableDir(file, null);
                }
                else {
                    for (PackageName packageName : includeDirs) {
                        if (packageName.isSub(file.getAbsolutePath().substring(srcDir.length() + 1))) {
                            tableDir(file, null);
                        }
                    }
                }
            }
        }
    }*/

    //add class unit as symbol to table
    /*void tableClass(TypeDeclaration<?> type, CompilationUnit cu) {
        if (cu.getPackageDeclaration().isPresent()) {
            table.addSymbol(cu.getPackageDeclaration().get().getNameAsString(), type.getNameAsString());
        }
        else {
            table.addSymbol("", type.getNameAsString());//no package
        }
        type.getMembers().forEach(m -> {
            if (m.isClassOrInterfaceDeclaration()) {
                tableClass(m.asClassOrInterfaceDeclaration(), cu);
            }
        });
        type.getMembers()//java8
                .stream()
                .filter(BodyDeclaration::isClassOrInterfaceDeclaration)
                .forEach(m -> tableClass(m.asClassOrInterfaceDeclaration(), cu));
    }*/

    String getPath(CompilationUnit cu) {
        if (cu.getPackage() != null) {
            return cu.getPackage().getName().getFullyQualifiedName().replace(".", "/");
        }
        /*if (cu.getPackageDeclaration().isPresent()) {
            String pkg = cu.getPackageDeclaration().get().getNameAsString();
            return pkg.replace(".", "/");
        }*/
        return "";
    }

    public void convertSingle(String path, CompilationUnit cu) {
        try {
            System.out.println("converting " + path);

            CHeader header = new CHeader(path.substring(0, path.length() - 4) + "h");
            CSource cpp = new CSource(header);

            header.addIncludeStar("java/lang");//by default as in java compilers
            header.addIncludeStar(getPath(cu));//visible to current package
            //header.useNamespace("java::lang");//by default

            MainVisitor visitor = new MainVisitor(this, header);
            cu.accept(visitor);
            //cu.types().forEach(type -> visitor.visit(type));

            String header_str = header.toString();
            String source_str = cpp.toString();

            if (debug_output) {
                System.out.println(header_str);
                System.out.println("---------------");
                System.out.println(source_str);
            }

            File header_file = new File(destDir, path.replace(".java", ".h"));
            File source_file = new File(destDir, path.replace(".java", ".cpp"));
            header_file.getParentFile().mkdirs();
            Files.write(Paths.get(header_file.getAbsolutePath()), header_str.getBytes());
            Files.write(Paths.get(source_file.getAbsolutePath()), source_str.getBytes());

            target.sourceFiles.add(Util.relative(source_file.getAbsolutePath(), srcDir));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void convertSingle(String cls) throws IOException {
        File file = new File(srcDir, cls);
        CompilationUnit unit = parse(file);
        convertSingle(cls, unit);


    }

    CompilationUnit parse(File file) throws IOException {
        parser.setSource(Util.read(file).toCharArray());
        parser.setUnitName(file.getPath());
        return (CompilationUnit) parser.createAST(null);
    }

}

