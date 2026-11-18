package com.mesut;

import org.junit.Test;

import javax.tools.ToolProvider;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;

/** Source subclasses of a compiled classpath base must link against the stub. */
public class ClasspathBaseTest {

    private static final String BASE = """
            package base;
            public class Base { public int value() { return 1; } }
            """;

    private static final String JAVA = """
            package ov;
            public class Ov extends base.Base {
                @Override public int value() { return 2; }
                public static int main(String[] a) { return new Ov().value() == 2 ? 1 : 0; }
            }
            """;

    private static final String MAIN = """
            #include "common.h"
            #include "ov/Ov.h"
            #include <cstdio>
            int main(){
                int r = ov::Ov::main(nullptr);
                printf("r=%d\\n", r);
                return r == 1 ? 0 : 1;
            }
            """;

    @Test
    public void classpathBaseStubIsIncludedAndLinks() throws Exception {
        Path libSrc = Files.createTempDirectory("j2cpp-cp-libsrc");
        Files.createDirectories(libSrc.resolve("base"));
        Path baseFile = libSrc.resolve("base/Base.java");
        Files.write(baseFile, BASE.getBytes(StandardCharsets.UTF_8));
        Path classes = Files.createTempDirectory("j2cpp-cp-classes");
        assertEquals("javac failed", 0, ToolProvider.getSystemJavaCompiler()
                .run(null, null, null, "-d", classes.toString(), baseFile.toString()));

        Path compiler = CppTestSupport.compiler();
        Path out = CppTestSupport.convertWithClasspath(JAVA, "ov", "Ov", "cp-base", classes);

        String header = CppTestSupport.generated(out, "ov/Ov.h");
        CppTestSupport.assertContains(header, "#include \"lib/base/Base.h\"");

        CppTestSupport.compile(out, compiler, "src/ov/Ov.cpp", MAIN, "cp_base_test")
                .assertOk("classpath base test");
        CppTestSupport.runMerged(out.resolve("cp_base_test")).assertOk("classpath base test");
    }
}
