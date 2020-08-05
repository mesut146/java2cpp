package com.mesut.j2cpp;


import com.mesut.j2cpp.ast.CHeader;
import com.mesut.j2cpp.ast.CSource;
import com.mesut.j2cpp.visitor.MainVisitor;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Converter {

    Resolver resolver;
    String srcDir;//source folder
    String destDir;//destinaytion folder for c++ files
    boolean includeAll = false;
    List<PackageName> includeDirs = new ArrayList<>();
    List<String> excludeDirs = new ArrayList<>();
    List<String> includeClasses = new ArrayList<>();
    List<String> excludeClasses = new ArrayList<>();
    List<PackageNode> packageHierarchy = new ArrayList<>();
    public List<String> classpath = new ArrayList<>();
    public CMakeWriter cMakeWriter;
    public CMakeWriter.Target target;
    public boolean debug_header = false, debug_source = false;
    public boolean debug_fields = false;
    public boolean debug_methods = false;
    ASTParser parser;
    int count = 0;

    public Converter(String srcDir, String destDir) {
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


    //jar or dir
    public void addClasspath(String path) {
        classpath.add(path);
    }

    @SuppressWarnings("rawtypes,unchecked")
    public void initParser() {
        if (parser != null) {
            return;
        }
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
        cpDirs.add(srcDir);
        parser.setEnvironment(cpJars.toArray(new String[0]), cpDirs.toArray(new String[0]), null, false);

        parser.setResolveBindings(true);
        parser.setBindingsRecovery(true);
        parser.setStatementsRecovery(true);

        Map options = JavaCore.getOptions();
        String ver = JavaCore.VERSION_13;
        options.put(JavaCore.COMPILER_COMPLIANCE, ver);
        options.put(JavaCore.COMPILER_SOURCE, ver);
        options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, ver);
        //options.put(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES,"true");
        parser.setCompilerOptions(options);
    }

    public void setDebugAll(boolean val) {
        setDebugSource(val);
        setDebugMembers(val);
    }

    public void setDebugMembers(boolean val) {
        debug_fields = val;
        debug_methods = val;
    }

    public void setDebugSource(boolean val) {
        debug_header = val;
        debug_source = val;
    }

    public void convert() {
        try {
            initParser();
            convertDir(new File(srcDir));
            writeCmake();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("conversion done for " + count + " files");
    }

    void convertDir(File dir) throws IOException {
        for (File file : Objects.requireNonNull(dir.listFiles())) {
            if (file.isDirectory()) {
                convertDir(file);
            }
            else if (file.getName().endsWith(".java")) {
                parser.setSource(Util.read(file).toCharArray());
                parser.setUnitName(file.getPath());
                CompilationUnit unit = (CompilationUnit) parser.createAST(null);
                convertSingle(Util.relative(file.getAbsolutePath(), srcDir), unit);
                count++;
            }
        }
    }


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

            /*HeaderWriter headerWriter = new HeaderWriter(cu);
            headerWriter.write();*/

            //make .h and .cpp
            MainVisitor visitor = new MainVisitor(this, header);
            cu.accept(visitor);

            String header_str = header.toString();
            String source_str = cpp.toString();

            if (debug_header) {
                System.out.println(header_str);

            }
            if (debug_source) {
                if (debug_header)
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
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        return (CompilationUnit) parser.createAST(null);
    }

    public void printNode(ASTNode node, StringBuilder sb) {
        sb.append(node.getClass());
        sb.append(" ");
    }

    public void writeCmake() throws IOException {
        String src = cMakeWriter.generate();
        File file = new File(destDir, "CMakeLists.txt");
        FileWriter writer = new FileWriter(file);
        writer.write(src);
        writer.close();
        System.out.println("cmake generation done");
    }

}

