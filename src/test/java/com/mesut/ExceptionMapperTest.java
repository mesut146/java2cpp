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

/** Tier 3d: Throwable/Exception map to a real class with message, cause and stack trace. */
public class ExceptionMapperTest {

    private static final String JAVA = """
            package e;
            public class E {
                static class Boom extends RuntimeException {
                    Boom(String m) { super(m); }
                }
                static void fail() { throw new Boom("bang"); }
                public static int main(String[] a) {
                    int r = 0;
                    try {
                        fail();
                    } catch (Exception e) {
                        if ("bang".equals(e.getMessage())) r += 1;
                        Boom cause = new Boom("cause");
                        e.initCause(cause);
                        if ("cause".equals(e.getCause().getMessage())) r += 2;
                        if (e.toString().contains("bang")) r += 4;
                        e.printStackTrace();
                    }
                    try {
                        throw new IllegalArgumentException("bad");
                    } catch (RuntimeException x) {
                        if ("bad".equals(x.getMessage())) r += 8;
                    }
                    return r;
                }
            }
            """;

    private static final String MAIN = """
            #include "common.h"
            #include "e/E.h"
            #include <cstdio>
            int main(){
                int r = e::E::main(nullptr);
                printf("r=%d\\n", r);
                return r == 15 ? 0 : 1;
            }
            """;

    @Test
    public void exceptionBehavior() throws Exception {
        Path compiler = Arrays.stream(System.getenv("PATH").split(":"))
                .map(Path::of).map(d -> d.resolve("g++")).filter(Files::isExecutable)
                .findFirst().orElse(null);
        Assume.assumeTrue("g++ is not available", compiler != null);

        Path src = Files.createTempDirectory("j2cpp-exm-src");
        Files.createDirectories(src.resolve("e"));
        Files.write(src.resolve("e/E.java"), JAVA.getBytes(StandardCharsets.UTF_8));
        Path out = Files.createTempDirectory("j2cpp-exm-out");
        Cmd.main(new String[]{"-src", src.toString(), "-out", out.toString()});

        String header = new String(Files.readAllBytes(out.resolve("e/E_Boom.h")), StandardCharsets.UTF_8);
        assertTrue("exception base not mapped:\n" + header,
                header.contains("public CppHelper::Throwable"));

        Path main = out.resolve("main.cpp");
        Files.write(main, MAIN.getBytes(StandardCharsets.UTF_8));
        Path exe = out.resolve("exm_test");
        Process cp = new ProcessBuilder(compiler.toString(), "-std=c++17",
                "-I" + Path.of("cppRt").toAbsolutePath(), "-I" + out,
                out.resolve("src/e/E.cpp").toString(), main.toString(), "-o", exe.toString())
                .redirectErrorStream(true).start();
        String compileLog = new String(cp.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals("compile/link failed:\n" + compileLog, 0,
                cp.waitFor(60, TimeUnit.SECONDS) ? cp.exitValue() : -1);

        Process run = new ProcessBuilder(exe.toString()).start();
        String stdout = new String(run.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        String stderr = new String(run.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals("exit code\n" + stderr, 0, run.waitFor(60, TimeUnit.SECONDS) ? run.exitValue() : -1);
        assertEquals("stdout", "r=15\n", stdout);
        assertTrue("stack trace missing:\n" + stderr, stderr.contains("java.lang.Throwable: bang"));
        assertTrue("cause missing:\n" + stderr, stderr.contains("Caused by:"));
    }
}
