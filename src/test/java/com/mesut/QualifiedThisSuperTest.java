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

/** Outer.this / Outer.super resolve through the enclosing-instance reference. */
public class QualifiedThisSuperTest {

    private static final String JAVA = """
            package q;
            class Base {
                int f = 1;
                String m() { return "base"; }
            }
            class Outer extends Base {
                String m() { return "outer"; }
                class Inner {
                    String test() { return Outer.this.m() + ":" + Outer.super.m(); }
                    int field() { return Outer.super.f + Outer.this.f; }
                }
            }
            public class Q {
                public static int main(String[] a) {
                    Outer o = new Outer();
                    String s = o.new Inner().test();
                    return s.equals("outer:base") ? 1 : 0;
                }
            }
            """;

    private static final String MAIN = """
            #include "common.h"
            #include "q/Q.h"
            #include <cstdio>
            int main(){
                int r = q::Q::main(nullptr);
                printf("r=%d\\n", r);
                return r == 1 ? 0 : 1;
            }
            """;

    @Test
    public void qualifiedThisAndSuperUseParentReference() throws Exception {
        Path compiler = Arrays.stream(System.getenv("PATH").split(":"))
                .map(Path::of).map(d -> d.resolve("g++")).filter(Files::isExecutable)
                .findFirst().orElse(null);
        Assume.assumeTrue("g++ is not available", compiler != null);

        Path src = Files.createTempDirectory("j2cpp-qts-src");
        Files.createDirectories(src.resolve("q"));
        Files.write(src.resolve("q/Q.java"), JAVA.getBytes(StandardCharsets.UTF_8));
        Path out = Files.createTempDirectory("j2cpp-qts-out");
        Cmd.main(new String[]{"-src", src.toString(), "-out", out.toString()});

        String log = new String(Files.readAllBytes(out.resolve("log.txt")), StandardCharsets.UTF_8);
        assertTrue("qualified this/super still logged as unsupported:\n" + log,
                !log.contains("qualified this is not supported")
                        && !log.contains("qualified super method invocation is not supported")
                        && !log.contains("qualified super field access is not supported"));

        String cpp = new String(Files.readAllBytes(out.resolve("src/q/Q.cpp")), StandardCharsets.UTF_8);
        assertTrue("outer this not resolved:\n" + cpp, cpp.contains("_parent_ref->m()"));
        assertTrue("qualified super method not resolved:\n" + cpp,
                cpp.contains("_parent_ref->q::Base::m()"));
        assertTrue("qualified super field not resolved:\n" + cpp,
                cpp.contains("_parent_ref->q::Base::f"));

        Path main = out.resolve("main.cpp");
        Files.write(main, MAIN.getBytes(StandardCharsets.UTF_8));
        Path exe = out.resolve("qts_test");
        java.util.List<String> command = new java.util.ArrayList<>();
        command.add(compiler.toString());
        command.add("-std=c++17");
        command.add("-I" + Path.of("cppRt").toAbsolutePath());
        command.add("-I" + out.toString());
        try (java.util.stream.Stream<Path> sources = Files.list(out.resolve("src/q"))) {
            sources.filter(p -> p.toString().endsWith(".cpp"))
                    .forEach(p -> command.add(p.toString()));
        }
        command.add(main.toString());
        command.add("-o");
        command.add(exe.toString());
        Process cp = new ProcessBuilder(command).redirectErrorStream(true).start();
        String compileLog = new String(cp.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals("compile/link failed:\n" + compileLog, 0,
                cp.waitFor(60, TimeUnit.SECONDS) ? cp.exitValue() : -1);

        Process run = new ProcessBuilder(exe.toString()).redirectErrorStream(true).start();
        String runLog = new String(run.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals("run output:\n" + runLog, 0, run.waitFor(60, TimeUnit.SECONDS) ? run.exitValue() : -1);
    }
}
