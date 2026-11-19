package com.mesut;

import org.junit.Test;

import javax.tools.ToolProvider;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;

/** One differential test for the second JRE batch (utilities, boxed types, random). */
public class JreBatchRuntimeTest {

    private static final String PROGRAM = """
            package b;
            import java.util.*;
            public class Big {
                static int test() {
                    Set<String> set = new HashSet<String>();
                    set.add("a");
                    boolean sc = set.contains("a");
                    int ss = set.size();
                    StringBuilder sb = new StringBuilder("x");
                    sb.append("y");
                    sb.append(1);
                    String s = sb.toString();
                    List<Integer> xs = Arrays.asList(1, 2, 3);
                    int asz = xs.size();
                    boolean eq = Objects.equals("a", "a");
                    int oh = Objects.hashCode("a");
                    Optional<String> o = Optional.of("v");
                    boolean op = o.isPresent();
                    boolean ov = "v".equals(o.orElse("d"));
                    Long l = 5L;
                    Double d = 2.5;
                    Float f = 1.5f;
                    Short sh = 2;
                    Byte by = 3;
                    Character c = 'x';
                    int mx = Math.max(1, 2);
                    Random rnd = new Random();
                    int ri = rnd.nextInt(10);
                    UUID u = UUID.randomUUID();
                    return ss + (sc ? 1 : 0) + s.length() + asz + (eq ? 1 : 0) + oh
                            + (op ? 1 : 0) + (ov ? 1 : 0) + l.intValue() + d.intValue()
                            + f.intValue() + sh.shortValue() + by.byteValue() + c.charValue()
                            + mx + (ri >= 0 && ri < 10 ? 1 : 0)
                            + (u.toString().length() == 36 ? 1 : 0)
                            + (Math.random() >= 0.0 ? 1 : 0);
                }
            }
            """;

    private static final String RUNNER = """
            package b;
            public class Runner {
                public static void main(String[] a) {
                    System.out.println(Big.test());
                }
            }
            """;

    private static final String MAIN = """
            #include "common.h"
            #include "b/Big.h"
            #include <cstdio>
            int main(){
                printf("%d\\n", b::Big::test());
                return 0;
            }
            """;

    @Test
    public void jreBatchMatchesJvm() throws Exception {
        Path javaSrc = Files.createTempDirectory("j2cpp-jrebatch-javasrc");
        Files.createDirectories(javaSrc.resolve("b"));
        Path file = javaSrc.resolve("b/Big.java");
        Files.write(file, PROGRAM.getBytes(StandardCharsets.UTF_8));
        Files.write(javaSrc.resolve("b/Runner.java"), RUNNER.getBytes(StandardCharsets.UTF_8));
        Path classes = Files.createTempDirectory("j2cpp-jrebatch-classes");
        assertEquals("javac failed", 0, ToolProvider.getSystemJavaCompiler()
                .run(null, null, null, "-d", classes.toString(), file.toString(),
                        javaSrc.resolve("b/Runner.java").toString()));
        Process jp = new ProcessBuilder("java", "-cp", classes.toString(), "b.Runner")
                .redirectErrorStream(true).start();
        String expected = new String(jp.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();

        Path compiler = CppTestSupport.compiler();
        Path out = CppTestSupport.convertNoMappers(PROGRAM, "b", "Big", "jrebatch");
        CppTestSupport.compile(out, compiler, "src/b/Big.cpp", MAIN, "jrebatch_test")
                .assertOk("JRE batch test");
        CppTestSupport.RunResult actual = CppTestSupport.runMerged(out.resolve("jrebatch_test"));
        actual.assertOk("JRE batch test");
        assertEquals("C++ output must match JVM:\n" + actual.output,
                expected, actual.output.trim());
    }
}
