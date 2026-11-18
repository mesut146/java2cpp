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

/** String/enum switches must honor break and fall-through; loop breaks stay in the loop. */
public class SwitchBreakTest {

    private static final String JAVA = """
            package s;
            public class S {
                enum Color { RED, GREEN, BLUE }
                public static int main(String[] a) {
                    int r = 0;
                    String x = "b";
                    switch (x) {
                        case "a": r += 1; break;
                        case "b": r += 2;
                        case "c": r += 4; break;
                        default: r += 8;
                    }
                    Color c = Color.GREEN;
                    switch (c) {
                        case RED: r += 16; break;
                        case GREEN: r += 32;
                        case BLUE: r += 64; break;
                    }
                    switch (x) {
                        case "b":
                            for (int i = 0; i < 3; i++) { break; }
                            r += 4;
                            break;
                        default: r += 8;
                    }
                    return r;
                }
            }
            """;

    private static final String MAIN = """
            #include "common.h"
            #include "s/S.h"
            #include <cstdio>
            int main(){
                int r = s::S::main(nullptr);
                printf("r=%d\\n", r);
                return r == 106 ? 0 : 1;
            }
            """;

    @Test
    public void switchBreakAndFallThrough() throws Exception {
        Path compiler = Arrays.stream(System.getenv("PATH").split(":"))
                .map(Path::of).map(d -> d.resolve("g++")).filter(Files::isExecutable)
                .findFirst().orElse(null);
        Assume.assumeTrue("g++ is not available", compiler != null);

        Path src = Files.createTempDirectory("j2cpp-swbr-src");
        Files.createDirectories(src.resolve("s"));
        Files.write(src.resolve("s/S.java"), JAVA.getBytes(StandardCharsets.UTF_8));
        Path out = Files.createTempDirectory("j2cpp-swbr-out");
        Cmd.main(new String[]{"-src", src.toString(), "-out", out.toString()});

        String gen = new String(Files.readAllBytes(out.resolve("src/s/S.cpp")), StandardCharsets.UTF_8);
        assertTrue("goto dispatch missing:\n" + gen, gen.contains("goto sw0_case_0"));
        assertTrue("break not redirected:\n" + gen, gen.contains("goto sw0_end"));
        assertTrue("for loop malformed:\n" + gen, gen.contains("auto i = 0;i<3;i++"));

        Path main = out.resolve("main.cpp");
        Files.write(main, MAIN.getBytes(StandardCharsets.UTF_8));
        Path exe = out.resolve("swbr_test");
        Process cp = new ProcessBuilder(compiler.toString(), "-std=c++17",
                "-I" + Path.of("cppRt").toAbsolutePath(), "-I" + out,
                out.resolve("src/s/S.cpp").toString(), main.toString(), "-o", exe.toString())
                .redirectErrorStream(true).start();
        String compileLog = new String(cp.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals("compile/link failed:\n" + compileLog, 0,
                cp.waitFor(60, TimeUnit.SECONDS) ? cp.exitValue() : -1);

        Process run = new ProcessBuilder(exe.toString()).redirectErrorStream(true).start();
        String log = new String(run.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals("run output:\n" + log, 0, run.waitFor(60, TimeUnit.SECONDS) ? run.exitValue() : -1);
    }
}
