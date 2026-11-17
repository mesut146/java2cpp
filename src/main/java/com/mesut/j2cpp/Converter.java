package com.mesut.j2cpp;


import com.mesut.j2cpp.ast.*;
import com.mesut.j2cpp.cppast.expr.CAssignment;
import com.mesut.j2cpp.cppast.expr.CFieldAccess;
import com.mesut.j2cpp.cppast.expr.CThisExpression;
import com.mesut.j2cpp.cppast.stmt.CBlockStatement;
import com.mesut.j2cpp.cppast.stmt.CExpressionStatement;
import com.mesut.j2cpp.map.ClassMap;
import com.mesut.j2cpp.util.*;
import com.mesut.j2cpp.visitor.*;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public class Converter {

    public List<String> classpath = new ArrayList<>();
    public CMakeWriter cMakeWriter;
    public CMakeWriter.Target target;
    Path srcDir;//source folder
    Path destDir;//destination folder for c++ files
    Path destSrc;
    Path headerDir;
    Filter filter;
    ASTParser parser;
    List<String> sourceList;
    int count = 0;
    CHeader forwardHeader;

    public Converter(String srcDir, String destDir) {
        this.srcDir = Paths.get(srcDir);
        this.destDir = Paths.get(destDir);
        this.destSrc = this.destDir.resolve("src");
        filter = new Filter(this.srcDir);
        forwardHeader = new CHeader("common.h");
        forwardHeader.forwardDeclarator = new ForwardDeclarator(ClassMap.sourceMap);
        try {
            Logger.init(this.destDir.resolve("log.txt"));
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
        parser = ASTParser.newParser(AST.getJLSLatest());
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
        cpDirs.add(srcDir.toString());
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
        cMakeWriter.sourceDir = destDir.toString();
        target = cMakeWriter.addTarget("mylib", false);
        target.addInclude(destDir.toString());
        target.addInclude(headerDir.toString());
        target.addInclude("lib");
    }

    public void stats() {
        try {
            Files.createDirectories(destDir);
            sourceList = new ArrayList<>();
            collect();
            System.out.println("total of " + sourceList.size() + " files");
            initParser();

            String[] b = new String[sourceList.size()];
            Arrays.fill(b, "");
            Stats stats = new Stats();
            parser.createASTs(sourceList.toArray(new String[0]), null, b, new FileASTRequestor() {
                @Override
                public void acceptAST(String sourceFilePath, CompilationUnit ast) {
                    ast.accept(stats);
                }
            }, null);
            stats.write(destDir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void convert() {
        try {
            Files.createDirectories(destDir);
            Files.createDirectories(destSrc);
            headerDir = Config.separateInclude ? destDir.resolve("include") : destDir;
            Files.createDirectories(headerDir);
            initCmake();
            initMain();
            preVisitDir();
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
            CType type = new CType(Config.mainClass);
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
                //source classes inherit from Object, which needs a complete type
                if (Config.writeLibHeader) {
                    forwardHeader.includes.add("lib/java/lang/Object.h");
                }
            }
            System.out.println("wrote " + forwardHeader.getInclude());
            Util.writeHeader(forwardHeader, headerDir);
        }
        if (Config.writeLibHeader) {
            LibHandler.instance.writeAll(Paths.get(headerDir.toString(), "lib"));
        }
    }

    private void convertDir() throws IOException {
        initParser();
        String[] b = new String[sourceList.size()];
        Arrays.fill(b, "");
        parser.createASTs(sourceList.toArray(new String[0]), null, b, new FileASTRequestor() {
            @Override
            public void acceptAST(String sourceFilePath, CompilationUnit ast) {
                Path path = Path.of(sourceFilePath);
                convertSingle(path, ast);
            }
        }, null);
    }

    private void preVisitDir() throws IOException {
        sourceList = new ArrayList<>();
        collect();
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
        System.out.println("pre visit done");
    }

    void collect() throws IOException {
        Files.walkFileTree(srcDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.toString().endsWith(".java") && filter.checkPath(file)) {
                    sourceList.add(file.toString());
                }
                return super.visitFile(file, attrs);
            }
        });
    }

    //create class-member model
    void preVisit(CompilationUnit cu) {
        PreVisitor visitor = new PreVisitor();
        visitor.handle(cu);
    }

    void convertSingle2(Path path, CompilationUnit cu) {
        try {
            Path relativePath = srcDir.relativize(path);
            System.out.println("converting " + relativePath);

            HeaderWriter hw = new HeaderWriter(headerDir);
            hw.all(cu);

            SourceVisitor sv = new SourceVisitor(destSrc, cu);
            sv.all(relativePath);

            if (Config.common_forwards) {
                //forwardHeader.forwardDeclarator.addAll(classes);
                if (Config.include_common_forwards) {
                    //source.includes.add(0, IncludeStmt.src(forwardHeader.getInclude()));
                }
            }
            //target.sourceFiles.add(source.name);
            count++;
        } catch (Exception e) {
            System.err.println("cant convert " + path);
            Logger.log(path + ":" + e.getMessage());
            e.printStackTrace();
        }
    }

    //convert bodies
    public void convertSingle(Path path, CompilationUnit cu) {
        convertSingle2(path, cu);
    }

    public void writeCmake() throws IOException {
        target.addInclude("lib");
        String src = cMakeWriter.generate();
        Path file = destDir.resolve("CMakeLists.txt");
        FileWriter writer = new FileWriter(file.toString());
        writer.write(src);
        writer.close();
        System.out.println("cmake generation done");
    }

}


