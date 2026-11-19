package com.mesut;

import org.junit.Test;

import javax.tools.ToolProvider;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;

/** Enhanced-for over JRE collections must match Java iteration behavior. */
public class JreIteratorRuntimeTest {

    private static final String PROGRAM = """
            package i;
            import java.util.*;
            public class I {
                static int test() {
                    List<String> xs = new ArrayList<String>();
                    xs.add("a"); xs.add("b");
                    int n = 0;
                    for (String x : xs) n += x.length();
                    Set<Integer> set = new HashSet<Integer>();
                    set.add(1); set.add(2);
                    for (Integer y : set) n += y;
                    return n;
                }
            }
            """;

    private static final String RUNNER = """
            package i;
            public class Runner {
                public static void main(String[] a) {
                    System.out.println(I.test());
                }
            }
            """;

    private static final String MAIN = """
            #include "common.h"
            #include "i/I.h"
            #include <cstdio>
            int main(){
                printf("%d\\n", i::I::test());
                return 0;
            }
            """;

    @Test
    public void forEachMatchesJvm() throws Exception {
        Path javaSrc = Files.createTempDirectory("j2cpp-jreitr-javasrc");
        Files.createDirectories(javaSrc.resolve("i"));
        Path file = javaSrc.resolve("i/I.java");
        Files.write(file, PROGRAM.getBytes(StandardCharsets.UTF_8));
        Files.write(javaSrc.resolve("i/Runner.java"), RUNNER.getBytes(StandardCharsets.UTF_8));
        Path classes = Files.createTempDirectory("j2cpp-jreitr-classes");
        assertEquals("javac failed", 0, ToolProvider.getSystemJavaCompiler()
                .run(null, null, null, "-d", classes.toString(), file.toString(),
                        javaSrc.resolve("i/Runner.java").toString()));
        Process jp = new ProcessBuilder("java", "-cp", classes.toString(), "i.Runner")
                .redirectErrorStream(true).start();
        String expected = new String(jp.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();

        Path compiler = CppTestSupport.compiler();
        Path out = CppTestSupport.convertNoMappers(PROGRAM, "i", "I", "jreitr");
        CppTestSupport.compile(out, compiler, "src/i/I.cpp", MAIN, "jreitr_test")
                .assertOk("JRE iterator test");
        CppTestSupport.RunResult actual = CppTestSupport.runMerged(out.resolve("jreitr_test"));
        actual.assertOk("JRE iterator test");
        assertEquals("C++ iteration output must match JVM:\n" + actual.output,
                expected, actual.output.trim());
    }
}
