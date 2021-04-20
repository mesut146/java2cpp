package com.mesut.j2cpp;


import com.mesut.j2cpp.ast.*;
import com.mesut.j2cpp.cppast.stmt.CBlockStatement;
import com.mesut.j2cpp.map.ClassMap;
import com.mesut.j2cpp.util.*;
import com.mesut.j2cpp.visitor.DeclarationVisitor;
import com.mesut.j2cpp.visitor.PreVisitor;
import com.mesut.j2cpp.visitor.SourceVisitor;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Converter {

    public List<String> classpath = new ArrayList<>();
    public CMakeWriter cMakeWriter;
    public CMakeWriter.Target target;
    String srcDir;//source folder
    String destDir;//destination folder for c++ files
    File headerDir;
    Filter filter;
    ASTParser parser;
    List<String> sourceList;
    int count = 0;
    CHeader forwardHeader;
    CHeader allHeader;

    public Converter(String srcDir, String destDir) {
        this.srcDir = srcDir;
        this.destDir = destDir;
        filter = new Filter(srcDir);
        allHeader = new CHeader("all.h");
        forwardHeader = new CHeader("common.h");
        forwardHeader.forwardDeclarator = new ForwardDeclarator(ClassMap.sourceMap);
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

    void initCmake() {
        cMakeWriter = new CMakeWriter("myproject");
        cMakeWriter.sourceDir = destDir;
        target = cMakeWriter.addTarget("mylib", false);
        target.addInclude(destDir);
        target.addInclude(headerDir.getAbsolutePath());
        target.addInclude("lib");
    }

    public void convert() {
        try {
            headerDir = Config.separateInclude ? new File(destDir, "include") : new File(destDir);
            headerDir.mkdirs();
            initCmake();
            initMain();
            preVisitDir();
            System.out.println("pre visit done");
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

    void initMain() {
        CClass cc;
        if (Config.mainClass != null) {
            CType type = new CType(Config.mainClass.replace(".", "::"));
            type.fromSource = true;
            cc = new CClass(type);
        }
        else {
            cc = new CClass();
            cc.name = "main";
        }
        //si_init
        CMethod method = new CMethod();
        cc.addMethod(method);
        method.body = new CBlockStatement();
        method.name = new CName("si_init");
        method.type = TypeHelper.getVoidType();
        method.setStatic(true);
        method.setPublic(true);
        ClassMap.sourceMap.mainClass = cc;
    }

    void writeForwards() throws IOException {
        if (Config.common_forwards) {
            if (Config.include_common_forwards) {
                forwardHeader.includes.add("lib/lib_common.h");
            }
            System.out.println("wrote " + forwardHeader.getInclude());
            Util.writeHeader(forwardHeader, headerDir);
        }
        if (Config.all_headers) {
            Util.writeHeader(allHeader, headerDir);
            System.out.println("wrote " + allHeader.getInclude());
        }
        if (Config.writeLibHeader) {
            LibHandler.instance.writeAll(new File(headerDir, "lib"));
        }
    }

    private void convertDir() throws IOException {
        initParser();
        String[] b = new String[sourceList.size()];
        Arrays.fill(b, "");
        parser.createASTs(sourceList.toArray(new String[0]), null, b, new FileASTRequestor() {
            @Override
            public void acceptAST(String sourceFilePath, CompilationUnit ast) {
                convertSingle(sourceFilePath, ast);
            }
        }, null);
    }

    private void preVisitDir() throws IOException {
        sourceList = new ArrayList<>();
        collect(new File(srcDir));
        System.out.println("total of " + sourceList.size() + " files");
        initParser();
        String[] b = new String[sourceList.size()];
        Arrays.fill(b, "");
        parser.createASTs(sourceList.toArray(new String[0]), null, b, new FileASTRequestor() {
            @Override
            public void acceptAST(String sourceFilePath, CompilationUnit ast) {
                preVisit(ast);
            }
        }, null);
    }

    void collect(File dir) {
        if (dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                if (file.isDirectory()) {
                    collect(file);
                }
                else if (file.getName().endsWith(".java")) {
                    if (filter.checkPath(file)) {
                        sourceList.add(file.getAbsolutePath());
                    }
                }
            }
        }
    }

    //create class-member model
    void preVisit(CompilationUnit cu) {
        PreVisitor visitor = new PreVisitor();
        visitor.handle(cu);
    }

    //convert bodies
    public void convertSingle(String path, CompilationUnit cu) {
        try {
            String relPath = Util.trimPrefix(path, srcDir);
            relPath = Util.trimPrefix(relPath, "/");
            System.out.println("converting " + relPath);
            CSource source = new CSource();
            source.name = Util.trimSuffix(relPath, ".java") + ".cpp";

            SourceVisitor sourceVisitor = new SourceVisitor(source);
            DeclarationVisitor headerVisitor = new DeclarationVisitor(sourceVisitor);

            List<CClass> classes = headerVisitor.classes;
            headerVisitor.convert(cu);
            sourceVisitor.convert(classes);
            source.classes.addAll(classes);
            Namespace ns = headerVisitor.ns;

            if (Config.all_headers && Config.include_all_headers) {
                source.includes.add(0, IncludeStmt.src(allHeader.getInclude()));
            }

            source.useNamespace(ns);

            for (CClass cc : classes) {
                cc.ns = ns;
                CHeader header = new CHeader(cc.getHeaderPath());
                header.setNs(ns);
                header.setClass(cc);
                source.includes.add(IncludeStmt.src(header.getInclude()));
                new HeaderDeps(header).handle();
                writeHeader(header);
            }

            if (Config.common_forwards) {
                forwardHeader.forwardDeclarator.addAll(classes);
                if (Config.include_common_forwards) {
                    source.includes.add(0, IncludeStmt.src(forwardHeader.getInclude()));
                }
            }
            handleDeps(source);
            Util.writeSource(source, new File(destDir));

            target.sourceFiles.add(source.name);
            count++;
        } catch (Exception e) {
            System.err.println("cant convert " + path);
            Logger.log(path + ":" + e.getMessage());
            e.printStackTrace();
        }
    }

    void handleDeps(CSource source) {
        //source deps
        Set<CClass> set = new HashSet<>();
        for (CClass cc : source.classes) {
            for (CType type : cc.types) {
                CClass t = ClassMap.sourceMap.get(type);
                if (t == null) continue;
                set.add(t);
            }
        }
        try {
            List<CClass> list = new ArrayList<>(set);
            BaseClassSorter.sort(list);
            for (CClass cc : list) {
                source.includes.add(cc.getType());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void writeHeader(CHeader header) throws IOException {
        Util.writeHeader(header, headerDir);
        if (Config.all_headers) {
            allHeader.includes.add(header.getInclude());
        }
    }

    public void writeCmake() throws IOException {
        target.addInclude("lib");
        String src = cMakeWriter.generate();
        File file = new File(destDir, "CMakeLists.txt");
        FileWriter writer = new FileWriter(file);
        writer.write(src);
        writer.close();
        System.out.println("cmake generation done");
    }

}


