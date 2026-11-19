package com.mesut;

import org.junit.Test;

import javax.tools.ToolProvider;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;

/** Exceptions in -no-mappers mode must carry message, cause and suppressed data. */
public class JreExceptionRuntimeTest {

    private static final String PROGRAM = """
            package e;
            public class E {
                static int test() {
                    int r = 0;
                    try {
                        throw new RuntimeException("boom");
                    } catch (RuntimeException ex) {
                        if ("boom".equals(ex.getMessage())) r += 1;
                        ex.initCause(new IllegalStateException("cause"));
                        if ("cause".equals(ex.getCause().getMessage())) r += 2;
                        ex.addSuppressed(new IllegalArgumentException("supp"));
                        if (ex.getSuppressed().length == 1) r += 4;
                        if (ex.toString().contains("boom")) r += 8;
                    }
                    try {
                        throw new IllegalArgumentException("bad");
                    } catch (Exception ex) {
                        if ("bad".equals(ex.getMessage())) r += 16;
                    }
                    return r;
                }
            }
            """;

    private static final String RUNNER = """
            package e;
            public class Runner {
                public static void main(String[] a) {
                    System.out.println(E.test());
                }
            }
            """;

    private static final String MAIN = """
            #include "common.h"
            #include "e/E.h"
            #include <cstdio>
            int main(){
                printf("%d\\n", e::E::test());
                return 0;
            }
            """;

    @Test
    public void exceptionsMatchJvm() throws Exception {
        Path javaSrc = Files.createTempDirectory("j2cpp-jreex-javasrc");
        Files.createDirectories(javaSrc.resolve("e"));
        Path file = javaSrc.resolve("e/E.java");
        Files.write(file, PROGRAM.getBytes(StandardCharsets.UTF_8));
        Files.write(javaSrc.resolve("e/Runner.java"), RUNNER.getBytes(StandardCharsets.UTF_8));
        Path classes = Files.createTempDirectory("j2cpp-jreex-classes");
        assertEquals("javac failed", 0, ToolProvider.getSystemJavaCompiler()
                .run(null, null, null, "-d", classes.toString(), file.toString(),
                        javaSrc.resolve("e/Runner.java").toString()));
        Process jp = new ProcessBuilder("java", "-cp", classes.toString(), "e.Runner")
                .redirectErrorStream(true).start();
        String expected = new String(jp.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();

        Path compiler = CppTestSupport.compiler();
        Path out = CppTestSupport.convertNoMappers(PROGRAM, "e", "E", "jreex");
        CppTestSupport.compile(out, compiler, "src/e/E.cpp", MAIN, "jreex_test")
                .assertOk("JRE exception test");
        CppTestSupport.RunResult actual = CppTestSupport.runMerged(out.resolve("jreex_test"));
        actual.assertOk("JRE exception test");
        assertEquals("C++ exception output must match JVM:\n" + actual.output,
                expected, actual.output.trim());
    }
}
