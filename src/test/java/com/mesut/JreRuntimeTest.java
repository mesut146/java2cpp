package com.mesut;

import org.junit.Test;

import javax.tools.ToolProvider;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/** First -no-mappers runtime slice: String/Integer/ArrayList behave like Java. */
public class JreRuntimeTest {

    private static final String JAVA = """
            package p;
            import java.util.*;
            public class P {
                static int test() {
                    String s = "abc";
                    int n = s.length();
                    String t = s.substring(1);
                    List<String> l = new ArrayList<String>();
                    l.add("x");
                    int sz = l.size();
                    Integer i = 5;
                    int x = i;
                    return n + sz + (t.equals("bc") ? 1 : 0) + x;
                }
            }
            """;

    private static final String RUNNER = """
            package p;
            public class Runner {
                public static void main(String[] a) {
                    System.out.println(P.test());
                }
            }
            """;

    private static final String MAIN = """
            #include "common.h"
            #include "p/P.h"
            #include <cstdio>
            int main(){
                printf("%d\\n", p::P::test());
                return 0;
            }
            """;

    @Test
    public void noMappersProgramMatchesJava() throws Exception {
        // ground truth from the real JVM
        Path javaSrc = Files.createTempDirectory("j2cpp-jre-javasrc");
        Files.createDirectories(javaSrc.resolve("p"));
        Path javaFile = javaSrc.resolve("p/P.java");
        Files.write(javaFile, JAVA.getBytes(StandardCharsets.UTF_8));
        Files.write(javaSrc.resolve("p/Runner.java"), RUNNER.getBytes(StandardCharsets.UTF_8));
        Path classes = Files.createTempDirectory("j2cpp-jre-classes");
        assertEquals("javac failed", 0, ToolProvider.getSystemJavaCompiler()
                .run(null, null, null, "-d", classes.toString(), javaFile.toString(),
                        javaSrc.resolve("p/Runner.java").toString()));
        Process jp = new ProcessBuilder("java", "-cp", classes.toString(), "p.Runner")
                .redirectErrorStream(true).start();
        String expected = new String(jp.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();

        Path compiler = CppTestSupport.compiler();
        Path out = CppTestSupport.convertNoMappers(JAVA, "p", "P", "jre");

        String cpp = CppTestSupport.generated(out, "src/p/P.cpp");
        CppTestSupport.assertContains(cpp, "#include \"jre/java/lang/String.h\"");
        CppTestSupport.assertContains(cpp, "#include \"jre/java/util/ArrayList.h\"");
        assertFalse("generated String stub still emitted", Files.exists(out.resolve("lib/java/lang/String.h")));

        CppTestSupport.compile(out, compiler, "src/p/P.cpp", MAIN, "jre_test")
                .assertOk("JRE test");
        CppTestSupport.RunResult actual = CppTestSupport.runMerged(out.resolve("jre_test"));
        actual.assertOk("JRE test");
        assertEquals("C++ output must match Java:\n" + actual.output,
                expected, actual.output.trim());
    }
}
