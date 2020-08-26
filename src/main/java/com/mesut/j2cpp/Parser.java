package com.mesut.j2cpp;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//cached parser
public class Parser {
    Map<String, CompilationUnit> map = new HashMap<>();
    ASTParser parser;
    Converter converter;

    public Parser(Converter converter) {
        this.converter = converter;
    }

    public void initParser() {
        parser = ASTParser.newParser(AST.JLS13);
        List<String> cpDirs = new ArrayList<>();
        List<String> cpJars = new ArrayList<>();

        for (String path : converter.classpath) {
            if (path.endsWith(".jar")) {
                cpJars.add(path);
            }
            else {
                cpDirs.add(path);
            }
        }
        cpDirs.add(converter.srcDir);
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


    public CompilationUnit parse(File file) throws IOException {
        if (map.containsKey(file.getAbsolutePath())) {
            return map.get(file.getAbsolutePath());
        }
        initParser();
        parser.setSource(Util.read(file).toCharArray());
        parser.setUnitName(file.getPath());
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        CompilationUnit unit = (CompilationUnit) parser.createAST(null);
        map.put(file.getAbsolutePath(), unit);
        return unit;
    }
}
