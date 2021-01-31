package com.mesut.j2cpp;


import com.mesut.j2cpp.ast.CClass;
import com.mesut.j2cpp.ast.CHeader;
import com.mesut.j2cpp.ast.CSource;
import com.mesut.j2cpp.util.LocalForwardDeclarator;
import com.mesut.j2cpp.util.Filter;
import com.mesut.j2cpp.visitor.DeclarationVisitor;
import com.mesut.j2cpp.visitor.SourceVisitor;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Converter {

    public List<String> classpath = new ArrayList<>();
    public CMakeWriter cMakeWriter;
    public CMakeWriter.Target target;
    public boolean debug_fields = false;
    public boolean debug_methods = false;
    public boolean stopOnError = false;
    String srcDir;//source folder
    String destDir;//destination folder for c++ files
    File headerDir;
    Filter filter;
    ASTParser parser;
    int count = 0;
    ClassMap classMap;
    CHeader forwardHeader;
    CHeader commonHeader;
    CHeader commonLibForwardHeader;
    CHeader commonLibHeader;

    public Converter(String srcDir, String destDir) {
        this.srcDir = srcDir;
        this.destDir = destDir;
        filter = new Filter(srcDir);
        cMakeWriter = new CMakeWriter("myproject");
        cMakeWriter.sourceDir = destDir;
        target = cMakeWriter.addTarget("mylib", false);
        classMap = new ClassMap();
        commonHeader = new CHeader("all.h");
        forwardHeader = new CHeader("common.h");
        forwardHeader.forwardDeclarator = new LocalForwardDeclarator(classMap);

        commonLibForwardHeader = new CHeader("lib_common.h");
        commonLibHeader = new CHeader("lib_all.h");
        try {
            Logger.init(new File(destDir, "log.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
    }

    public void setDebugMembers(boolean val) {
        debug_fields = val;
        debug_methods = val;
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
            convertDir();
            writeCmake();
            writeForwards();
            if (Logger.hasErrors) {
                System.err.println("conversion has errors check logs");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("conversion done for " + count + " files");
    }

    void writeForwards() throws IOException {
        if (Config.common_forwards) {
            File file = new File(headerDir, forwardHeader.rpath);
            Files.write(file.toPath(), forwardHeader.toString().getBytes());
        }
        if (Config.common_headers) {
            File file = new File(headerDir, commonHeader.rpath);
            Files.write(file.toPath(), commonHeader.toString().getBytes());
        }

        if (Config.writeLibHeader) {
            LibImplHandler.instance.writeAll(new File(headerDir, "lib"), commonLibForwardHeader, commonLibHeader);
        }
    }

    private void convertDir() throws IOException {
        List<String> list = new ArrayList<>();
        collect(new File(srcDir), list);
        initParser();
        String[] b = new String[list.size()];
        Arrays.fill(b, "");
        parser.createASTs(list.toArray(new String[0]), null, b, new FileASTRequestor() {
            @Override
            public void acceptAST(String sourceFilePath, CompilationUnit ast) {
                convertSingle(sourceFilePath, ast);
            }
        }, null);
    }

    void collect(File dir, List<String> list) {
        if (dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                if (file.isDirectory()) {
                    collect(file, list);
                }
                else if (file.getName().endsWith(".java")) {
                    if (filter.checkPath(file)) {
                        list.add(file.getAbsolutePath());
                    }
                }
            }
        }
    }

    public void convertSingle(String cls) throws IOException {
        File file = new File(srcDir, cls);
        CompilationUnit unit = parse(file);
        convertSingle(cls, unit);
    }

    public void convertSingle(String path, CompilationUnit cu) {
        try {
            String relPath = Util.trimPrefix(path, srcDir);
            relPath = Util.trimPrefix(relPath, "/");
            //System.out.println("converting " + relPath);
            CHeader header = new CHeader(Util.trimSuffix(relPath, "java") + "h");
            CSource source = new CSource(header);

            SourceVisitor sourceVisitor = new SourceVisitor(source);
            DeclarationVisitor headerVisitor = new DeclarationVisitor(sourceVisitor);

            headerVisitor.convert(cu);
            sourceVisitor.convert();

            if (Config.common_headers && Config.include_common_headers) {
                source.includes.add(0, commonHeader.getInclude());
            }

            if (Config.move_inners_out) {
                for (int i = 0; i < header.classes.size(); i++) {
                    CClass cc = header.classes.get(i);
                    String inner_path;
                    if (i == 0) {
                        inner_path = header.getInclude();
                    }
                    else {
                        inner_path = Util.trimSuffix(relPath, ".java") + "_" + cc.name + ".h";
                    }
                    CHeader hh = new CHeader(inner_path);
                    hh.setNs(header.ns);
                    hh.addClass(cc);
                    source.addInclude(hh.getInclude());
                    writeHeader(hh);
                }
            }
            else {
                writeHeader(header);
            }
            classMap.addAll(header.classes);
            if (Config.common_forwards) {
                forwardHeader.forwardDeclarator.addAll(header.classes);
                if (Config.include_common_forwards) {
                    source.includes.add(0, forwardHeader.rpath);
                }
            }
            //new BaseForward(header).sort();

            File source_file = new File(destDir, relPath.replace(".java", ".cpp"));
            source_file.getParentFile().mkdirs();
            Files.write(source_file.toPath(), source.toString().getBytes());

            target.addInclude(destDir);
            target.addInclude(headerDir.getAbsolutePath());//todo once
            target.sourceFiles.add(source_file.getAbsolutePath());
            count++;
        } catch (Exception e) {
            System.err.println("cant convert " + path);
            e.printStackTrace();
        }
    }

    void writeHeader(CHeader header) throws IOException {
        File header_file = new File(headerDir, header.getInclude().replace(".java", ".h"));
        header_file.getParentFile().mkdirs();
        Files.write(header_file.toPath(), header.toString().getBytes());

        if (Config.common_headers) {
            commonHeader.addInclude(header.getInclude());
        }
    }

    CompilationUnit parse(File file) throws IOException {
        initParser();
        parser.setSource(Util.read(file).toCharArray());
        parser.setUnitName(file.getPath());
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


