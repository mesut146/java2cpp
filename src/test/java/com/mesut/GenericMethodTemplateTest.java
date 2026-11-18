package com.mesut;

import com.mesut.j2cpp.Cmd;
import org.junit.Assume;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/** Stage 2: method type parameters become C++ templates and T is deduced at call sites. */
public class GenericMethodTemplateTest {

    private static final String JAVA = """
            package g;
            public class G {
                public static <T> T identity(T x) { return x; }
                public static <T> T pick(T a, T b) { return a; }
                public static int main(String[] a) {
                    int v = identity(5);
                    String s = identity("ok");
                    int p = pick(3, 9);
                    return v + p + (s.equals("ok") ? 1 : 0);
                }
            }
            """;

    private static final String MAIN = """
            #include "common.h"
            #include "g/G.h"
            #include <cstdio>
            int main(){
                int r = g::G::main(nullptr);
                printf("r=%d\\n", r);
                return r == 9 ? 0 : 1;
            }
            """;

    @Test
    public void genericMethodsCompileAndRun() throws Exception {
        Path compiler = Arrays.stream(System.getenv("PATH").split(":"))
                .map(Path::of).map(d -> d.resolve("g++")).filter(Files::isExecutable)
                .findFirst().orElse(null);
        Assume.assumeTrue("g++ is not available", compiler != null);

        Path src = Files.createTempDirectory("j2cpp-gtm-src");
        Files.createDirectories(src.resolve("g"));
        Files.write(src.resolve("g/G.java"), JAVA.getBytes(StandardCharsets.UTF_8));
        Path out = Files.createTempDirectory("j2cpp-gtm-out");
        Cmd.main(new String[]{"-src", src.toString(), "-out", out.toString()});

        String header = new String(Files.readAllBytes(out.resolve("g/G.h")), StandardCharsets.UTF_8);
        assertTrue("templates missing:\n" + header, header.contains("template <class T>"));
        assertTrue("type variable not preserved:\n" + header, header.contains("static T identity(T x)"));

        Path main = out.resolve("main.cpp");
        Files.write(main, MAIN.getBytes(StandardCharsets.UTF_8));
        Path exe = out.resolve("gtm_test");
        Process cp = new ProcessBuilder(compiler.toString(), "-std=c++17",
                "-I" + Path.of("cppRt").toAbsolutePath(), "-I" + out,
                out.resolve("src/g/G.cpp").toString(), main.toString(), "-o", exe.toString())
                .redirectErrorStream(true).start();
        String compileLog = new String(cp.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals("compile/link failed:\n" + compileLog, 0,
                cp.waitFor(60, TimeUnit.SECONDS) ? cp.exitValue() : -1);

        Process run = new ProcessBuilder(exe.toString()).redirectErrorStream(true).start();
        String log = new String(run.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals("run output:\n" + log, 0, run.waitFor(60, TimeUnit.SECONDS) ? run.exitValue() : -1);
    }
}
