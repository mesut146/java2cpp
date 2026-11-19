package com.mesut;

import org.junit.Test;

import javax.tools.ToolProvider;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;

/** System.out/err in -no-mappers mode must match JVM console output. */
public class JreConsoleRuntimeTest {

    private static final String JAVA = """
            package s;
            public class S {
                public static void main(String[] a) {
                    System.out.println("hello");
                    System.out.println(5);
                    System.out.print("x");
                    System.out.println(3.5);
                    System.err.println("err");
                }
            }
            """;

    private static final String MAIN = """
            #include "common.h"
            #include "s/S.h"
            int main(){
                s::S::main(nullptr);
                return 0;
            }
            """;

    @Test
    public void consoleOutputMatchesJvm() throws Exception {
        Path javaSrc = Files.createTempDirectory("j2cpp-jrecon-javasrc");
        Files.createDirectories(javaSrc.resolve("s"));
        Path file = javaSrc.resolve("s/S.java");
        Files.write(file, JAVA.getBytes(StandardCharsets.UTF_8));
        Path classes = Files.createTempDirectory("j2cpp-jrecon-classes");
        assertEquals("javac failed", 0, ToolProvider.getSystemJavaCompiler()
                .run(null, null, null, "-d", classes.toString(), file.toString()));

        Path compiler = CppTestSupport.compiler();
        Path out = CppTestSupport.convertNoMappers(JAVA, "s", "S", "jrecon");

        String cpp = CppTestSupport.generated(out, "src/s/S.cpp");
        CppTestSupport.assertContains(cpp, "#include \"jre/java/lang/System.h\"");

        CppTestSupport.compile(out, compiler, "src/s/S.cpp", MAIN, "jrecon_test")
                .assertOk("JRE console test");
        CppTestSupport.RunResult actual = CppTestSupport.runMerged(out.resolve("jrecon_test"));
        actual.assertOk("JRE console test");

        //the JVM's stdout and stderr are merged for the comparison
        Process jp = new ProcessBuilder("java", "-cp", classes.toString(), "s.S")
                .redirectErrorStream(true).start();
        String expected = new String(jp.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
        assertEquals("C++ console output must match JVM:\n" + actual.output,
                expected, actual.output.trim());
    }
}
