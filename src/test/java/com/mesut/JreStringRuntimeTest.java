package com.mesut;

import org.junit.Test;

import javax.tools.ToolProvider;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;

/** Java String semantics in -no-mappers mode must match the JVM. */
public class JreStringRuntimeTest {

    private static final String PROGRAM = """
            package t;
            public class T {
                static int test() {
                    String s = "a,b,";
                    int h = "hello".hashCode();
                    String sub = s.substring(1, 3);
                    int cmp = "abc".compareTo("abd");
                    boolean eq = "café".equals(new String("café"));
                    String[] parts = s.split(",", -1);
                    String r = s.replace('a', 'X').replace(",", ";");
                    String u = "Abc".toUpperCase();
                    String j = String.join("-", "a", "b");
                    String tr = "  hi  ".trim();
                    boolean bl = "  ".isBlank();
                    int idx = "hello".indexOf("ll");
                    boolean ci = "Hello".equalsIgnoreCase("hello");
                    return h + sub.length() + cmp + (eq ? 1 : 0) + parts.length
                            + r.length() + u.length() + j.length() + tr.length()
                            + (bl ? 1 : 0) + idx + (ci ? 1 : 0)
                            + "world".lastIndexOf('o');
                }
            }
            """;

    private static final String RUNNER = """
            package t;
            public class Runner {
                public static void main(String[] a) {
                    System.out.println(T.test());
                }
            }
            """;

    private static final String MAIN = """
            #include "common.h"
            #include "t/T.h"
            #include <cstdio>
            int main(){
                printf("%d\\n", t::T::test());
                return 0;
            }
            """;

    @Test
    public void stringSemanticsMatchJvm() throws Exception {
        Path javaSrc = Files.createTempDirectory("j2cpp-jrestr-javasrc");
        Files.createDirectories(javaSrc.resolve("t"));
        Path file = javaSrc.resolve("t/T.java");
        Files.write(file, PROGRAM.getBytes(StandardCharsets.UTF_8));
        Path runner = javaSrc.resolve("t/Runner.java");
        Files.write(runner, RUNNER.getBytes(StandardCharsets.UTF_8));
        Path classes = Files.createTempDirectory("j2cpp-jrestr-classes");
        assertEquals("javac failed", 0, ToolProvider.getSystemJavaCompiler()
                .run(null, null, null, "-encoding", "UTF-8", "-d", classes.toString(),
                        file.toString(), runner.toString()));
        Process jp = new ProcessBuilder("java", "-cp", classes.toString(), "t.Runner")
                .redirectErrorStream(true).start();
        String expected = new String(jp.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();

        Path compiler = CppTestSupport.compiler();
        Path out = CppTestSupport.convertNoMappers(PROGRAM, "t", "T", "jrestr");
        CppTestSupport.compile(out, compiler, "src/t/T.cpp", MAIN, "jrestr_test")
                .assertOk("JRE string test");
        CppTestSupport.RunResult actual = CppTestSupport.runMerged(out.resolve("jrestr_test"));
        actual.assertOk("JRE string test");
        assertEquals("C++ String output must match JVM:\n" + actual.output,
                expected, actual.output.trim());
    }
}
