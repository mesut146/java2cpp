package com.mesut;

import org.junit.Test;

import javax.tools.ToolProvider;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;

/** HashMap in -no-mappers mode must behave like java.util.HashMap. */
public class JreMapRuntimeTest {

    private static final String PROGRAM = """
            package m;
            import java.util.*;
            public class M {
                static int test() {
                    Map<String, Integer> map = new HashMap<String, Integer>();
                    map.put("a", 1);
                    map.put("b", 2);
                    int s = map.size();
                    Integer v = map.get("a");
                    boolean has = map.containsKey("b");
                    boolean missing = map.containsKey("x");
                    Integer old = map.put("a", 9);
                    map.remove("b");
                    return s + v + old + (has ? 1 : 0) + (missing ? 0 : 1)
                            + map.size() + map.get("a");
                }
            }
            """;

    private static final String RUNNER = """
            package m;
            public class Runner {
                public static void main(String[] a) {
                    System.out.println(M.test());
                }
            }
            """;

    private static final String MAIN = """
            #include "common.h"
            #include "m/M.h"
            #include <cstdio>
            int main(){
                printf("%d\\n", m::M::test());
                return 0;
            }
            """;

    @Test
    public void hashMapMatchesJvm() throws Exception {
        Path javaSrc = Files.createTempDirectory("j2cpp-jremap-javasrc");
        Files.createDirectories(javaSrc.resolve("m"));
        Path file = javaSrc.resolve("m/M.java");
        Files.write(file, PROGRAM.getBytes(StandardCharsets.UTF_8));
        Files.write(javaSrc.resolve("m/Runner.java"), RUNNER.getBytes(StandardCharsets.UTF_8));
        Path classes = Files.createTempDirectory("j2cpp-jremap-classes");
        assertEquals("javac failed", 0, ToolProvider.getSystemJavaCompiler()
                .run(null, null, null, "-d", classes.toString(), file.toString(),
                        javaSrc.resolve("m/Runner.java").toString()));
        Process jp = new ProcessBuilder("java", "-cp", classes.toString(), "m.Runner")
                .redirectErrorStream(true).start();
        String expected = new String(jp.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();

        Path compiler = CppTestSupport.compiler();
        Path out = CppTestSupport.convertNoMappers(PROGRAM, "m", "M", "jremap");
        CppTestSupport.compile(out, compiler, "src/m/M.cpp", MAIN, "jremap_test")
                .assertOk("JRE map test");
        CppTestSupport.RunResult actual = CppTestSupport.runMerged(out.resolve("jremap_test"));
        actual.assertOk("JRE map test");
        assertEquals("C++ map output must match JVM:\n" + actual.output,
                expected, actual.output.trim());
    }
}
