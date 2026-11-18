package com.mesut;

import org.junit.Test;

import javax.tools.ToolProvider;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;

/** Legacy raw generic types from classpath bindings must get valid C++ templates. */
public class RawGenericClasspathTest {

    private static final String LIB = """
            package lib;
            import java.util.List;
            public class Legacy {
                public static List raw(List xs) { return xs; }
            }
            """;

    private static final String JAVA = """
            package use;
            public class U {
                public static int main(String[] a) {
                    return lib.Legacy.raw(null) == null ? 1 : 0;
                }
            }
            """;

    private static final String MAIN = """
            #include "common.h"
            #include "use/U.h"
            #include <cstdio>
            int main(){
                int r = use::U::main(nullptr);
                printf("r=%d\\n", r);
                return r == 1 ? 0 : 1;
            }
            """;

    @Test
    public void rawListFromClasspathIsTemplated() throws Exception {
        Path libSrc = Files.createTempDirectory("j2cpp-raw-libsrc");
        Files.createDirectories(libSrc.resolve("lib"));
        Path libFile = libSrc.resolve("lib/Legacy.java");
        Files.write(libFile, LIB.getBytes(StandardCharsets.UTF_8));
        Path classes = Files.createTempDirectory("j2cpp-raw-classes");
        assertEquals("javac failed", 0, ToolProvider.getSystemJavaCompiler()
                .run(null, null, null, "-d", classes.toString(), libFile.toString()));
        Path jar = Files.createTempDirectory("j2cpp-raw-jar").resolve("legacy.jar");
        assertEquals("jar failed", 0, new ProcessBuilder("jar", "cf", jar.toString(),
                "-C", classes.toString(), ".").redirectErrorStream(true).start().waitFor());

        Path compiler = CppTestSupport.compiler();
        Path out = CppTestSupport.convertWithClasspath(JAVA, "use", "U", "raw-generic", jar);

        String stub = CppTestSupport.generated(out, "lib/lib/Legacy.h");
        CppTestSupport.assertContains(stub,
                "static std::vector<java::lang::Object*>* raw(std::vector<java::lang::Object*>* p0)");

        CppTestSupport.compile(out, compiler, "src/use/U.cpp", MAIN, "raw_generic_test")
                .assertOk("raw generic test");
        CppTestSupport.runMerged(out.resolve("raw_generic_test")).assertOk("raw generic test");
    }
}
