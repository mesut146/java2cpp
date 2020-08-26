package com.mesut.j2cpp;


import com.mesut.j2cpp.ast.CHeader;
import com.mesut.j2cpp.ast.CSource;
import com.mesut.j2cpp.util.Filter;
import com.mesut.j2cpp.visitor.DeclarationVisitor;
import com.mesut.j2cpp.visitor.SourceVisitor;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Converter {

    public List<String> classpath = new ArrayList<>();
    public CMakeWriter cMakeWriter;
    public CMakeWriter.Target target;
    public boolean debug_header = false;
    public boolean debug_source = false;
    public boolean debug_fields = false;
    public boolean debug_methods = false;
    String srcDir;//source folder
    String destDir;//destination folder for c++ files
    File headerDir;
    Filter filter;
    ASTParser parser;
    int count = 0;

    public Converter(String srcDir, String destDir) {
        this.srcDir = srcDir;
        this.destDir = destDir;
        filter = new Filter(srcDir);
        cMakeWriter = new CMakeWriter("myproject");
        cMakeWriter.sourceDir = destDir;
        target = cMakeWriter.addTarget("mylib", false);
    }

    public Filter getFilter() {
        return filter;
    }

    //jar or dir
    public void addClasspath(String path) {
        classpath.add(path);
    }

    @SuppressWarnings("rawtypes,unchecked")
    public void initParser() {
        /*if (parser != null) {
            return;
        }*/
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
        parser.setEnvironment(cpJars.toArray(new String[0]), cpDirs.toArray(new String[0]), null, true);

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
            if (Config.separateInclude) {
                headerDir = new File(destDir, "include");
                headerDir.mkdirs();
            }
            else {
                headerDir = new File(destDir);
            }
            initParser();
            convertDir(new File(srcDir));
            writeCmake();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("conversion done for " + count + " files");
    }

    void convertDir(File dir) throws IOException {
        if (dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                if (file.isDirectory()) {
                    convertDir(file);
                }
                else if (file.getName().endsWith(".java")) {
                    convertSingle(file.getAbsolutePath().substring(srcDir.length() + 1));
                }
            }
        }

    }

    public void convertSingle(String cls) throws IOException {
        File file = new File(srcDir, cls);
        if (filter.checkPath(file)) {
            CompilationUnit unit = parse(file);
            convertSingle(cls, unit);
            count++;
        }
    }

    public void convertSingle(String path, CompilationUnit cu) {
        try {
            System.out.println("converting " + path);

            CHeader header = new CHeader(path.substring(0, path.length() - 4) + "h");
            CSource cpp = new CSource(header);

            /*HeaderWriter headerWriter = new HeaderWriter(cu);
            headerWriter.write();*/

            //make header
            SourceVisitor sourceVisitor = new SourceVisitor(cpp);
            DeclarationVisitor headerVisitor = new DeclarationVisitor(sourceVisitor);

            headerVisitor.convert(cu);
            sourceVisitor.convert();

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

            File header_file = new File(headerDir, path.replace(".java", ".h"));
            File source_file = new File(destDir, path.replace(".java", ".cpp"));
            header_file.getParentFile().mkdirs();
            source_file.getParentFile().mkdirs();
            Files.write(header_file.toPath(), header_str.getBytes());
            Files.write(source_file.toPath(), source_str.getBytes());

            target.addInclude(destDir);
            target.addInclude(headerDir.getAbsolutePath());
            target.sourceFiles.add(source_file.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    CompilationUnit parse(File file) throws IOException {
        initParser();
        parser.setSource(Util.read(file).toCharArray());
        parser.setUnitName(file.getPath());
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        return (CompilationUnit) parser.createAST(null);
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


