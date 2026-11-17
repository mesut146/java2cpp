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

/** Tier 3a: System.out/err must compile, link and print to real C++ streams. */
public class ConsoleIOTest {

    private static final String JAVA = """
            package p;
            public class P {
                static class A { public String toString() { return "A!"; } }
                public static void main(String[] a) {
                    System.out.print("x");
                    System.out.print(7);
                    System.out.println();
                    System.out.println(true);
                    System.out.println(3.5);
                    System.out.println(new A());
                    System.out.printf("%d-%s%n", 5, "ok");
                    System.err.println("err");
                }
            }
            """;

    private static final String MAIN = """
            #include "common.h"
            #include "p/P.h"
            int main(){ p::P::main(nullptr); return 0; }
            """;

    @Test
    public void consoleOutputLinksAndMatchesJava() throws Exception {
        Path compiler = Arrays.stream(System.getenv("PATH").split(":"))
                .map(Path::of).map(d -> d.resolve("g++")).filter(Files::isExecutable)
                .findFirst().orElse(null);
        Assume.assumeTrue("g++ is not available", compiler != null);

        Path src = Files.createTempDirectory("j2cpp-io-src");
        Files.createDirectories(src.resolve("p"));
        Files.write(src.resolve("p/P.java"), JAVA.getBytes(StandardCharsets.UTF_8));
        Path out = Files.createTempDirectory("j2cpp-io-out");
        Cmd.main(new String[]{"-src", src.toString(), "-out", out.toString()});

        String gen = new String(Files.readAllBytes(out.resolve("src/p/P.cpp")), StandardCharsets.UTF_8);
        assertTrue("System.out not mapped:\n" + gen, gen.contains("CppHelper::javaOut()"));
        assertTrue("System.err not mapped:\n" + gen, gen.contains("CppHelper::javaErr()"));

        Path main = out.resolve("main.cpp");
        Files.write(main, MAIN.getBytes(StandardCharsets.UTF_8));
        Path exe = out.resolve("io_test");
        Process cp = new ProcessBuilder(compiler.toString(), "-std=c++17",
                "-I" + Path.of("cppRt").toAbsolutePath(), "-I" + out,
                out.resolve("src/p/P.cpp").toString(), main.toString(), "-o", exe.toString())
                .redirectErrorStream(true).start();
        String compileLog = new String(cp.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals("compile/link failed:\n" + compileLog, 0,
                cp.waitFor(60, TimeUnit.SECONDS) ? cp.exitValue() : -1);

        Process run = new ProcessBuilder(exe.toString()).start();
        String stdout = new String(run.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        String stderr = new String(run.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals("exit code", 0, run.waitFor(60, TimeUnit.SECONDS) ? run.exitValue() : -1);
        assertEquals("stdout", "x7\ntrue\n3.5\nA!\n5-ok\n", stdout);
        assertEquals("stderr", "err\n", stderr);
    }
}
