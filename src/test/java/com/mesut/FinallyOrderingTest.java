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

/** Resources must be closed before an explicit finally, including on exception. */
public class FinallyOrderingTest {

    private static final String JAVA_TEMPLATE = """
            package f;
            import java.io.*;
            import java.nio.file.*;
            public class F {
                public static int main(String[] a) throws Exception {
                    int r = 0;
                    Path p = Paths.get("__BASE__/out.txt");
                    try {
                        try (PrintWriter pw = new PrintWriter("__BASE__/out.txt")) {
                            pw.println("body");
                            throw new RuntimeException("boom");
                        } finally {
                            String s = Files.readString(p);
                            if (s.equals("body\\n")) r += 1;
                        }
                    } catch (RuntimeException e) {
                        r += 2;
                    }
                    return r;
                }
            }
            """;

    private static final String MAIN = """
            #include "common.h"
            #include "f/F.h"
            #include <cstdio>
            int main(){
                int r = f::F::main(nullptr);
                printf("r=%d\\n", r);
                return r == 3 ? 0 : 1;
            }
            """;

    @Test
    public void resourcesCloseBeforeFinally() throws Exception {
        Path compiler = Arrays.stream(System.getenv("PATH").split(":"))
                .map(Path::of).map(d -> d.resolve("g++")).filter(Files::isExecutable)
                .findFirst().orElse(null);
        Assume.assumeTrue("g++ is not available", compiler != null);

        Path base = Files.createTempDirectory("j2cpp-finorder-base");
        String java = JAVA_TEMPLATE.replace("__BASE__", base.toString());

        Path src = Files.createTempDirectory("j2cpp-finorder-src");
        Files.createDirectories(src.resolve("f"));
        Files.write(src.resolve("f/F.java"), java.getBytes(StandardCharsets.UTF_8));
        Path out = Files.createTempDirectory("j2cpp-finorder-out");
        Cmd.main(new String[]{"-src", src.toString(), "-out", out.toString()});

        String gen = new String(Files.readAllBytes(out.resolve("src/f/F.cpp")), StandardCharsets.UTF_8);
        assertTrue("guard missing:\n" + gen, gen.contains("CppHelper::ScopeGuard"));
        int lambda = gen.indexOf("[&]() -> std::optional<int>");
        int guard = gen.indexOf("CppHelper::ScopeGuard");
        assertTrue("guard not inside try lambda:\n" + gen, lambda >= 0 && guard > lambda);

        Path main = out.resolve("main.cpp");
        Files.write(main, MAIN.getBytes(StandardCharsets.UTF_8));
        Path exe = out.resolve("finorder_test");
        Process cp = new ProcessBuilder(compiler.toString(), "-std=c++17",
                "-I" + Path.of("cppRt").toAbsolutePath(), "-I" + out,
                out.resolve("src/f/F.cpp").toString(), main.toString(), "-o", exe.toString())
                .redirectErrorStream(true).start();
        String compileLog = new String(cp.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals("compile/link failed:\n" + compileLog, 0,
                cp.waitFor(60, TimeUnit.SECONDS) ? cp.exitValue() : -1);

        Process run = new ProcessBuilder(exe.toString()).redirectErrorStream(true).start();
        String log = new String(run.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals("run output:\n" + log, 0, run.waitFor(60, TimeUnit.SECONDS) ? run.exitValue() : -1);
    }
}
