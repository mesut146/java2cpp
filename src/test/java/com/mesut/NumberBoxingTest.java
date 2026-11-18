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

/** A Number loop variable over List<Integer> must box the mapped primitive element. */
public class NumberBoxingTest {

    private static final String JAVA = """
            package n;
            import java.util.*;
            public class N {
                public static int sum(List<? extends Number> xs) {
                    int r = 0;
                    for (Number x : xs) r += x.intValue();
                    return r;
                }
                public static int main(String[] a) {
                    List<Integer> l = new ArrayList<Integer>();
                    l.add(1); l.add(2); l.add(3);
                    return sum(l);
                }
            }
            """;

    private static final String MAIN = """
            #include "common.h"
            #include "n/N.h"
            #include <cstdio>
            int main(){
                int r = n::N::main(nullptr);
                printf("r=%d\\n", r);
                return r == 6 ? 0 : 1;
            }
            """;

    @Test
    public void numberLoopVariableBoxesElements() throws Exception {
        Path compiler = Arrays.stream(System.getenv("PATH").split(":"))
                .map(Path::of).map(d -> d.resolve("g++")).filter(Files::isExecutable)
                .findFirst().orElse(null);
        Assume.assumeTrue("g++ is not available", compiler != null);

        Path src = Files.createTempDirectory("j2cpp-num-src");
        Files.createDirectories(src.resolve("n"));
        Files.write(src.resolve("n/N.java"), JAVA.getBytes(StandardCharsets.UTF_8));
        Path out = Files.createTempDirectory("j2cpp-num-out");
        Cmd.main(new String[]{"-src", src.toString(), "-out", out.toString()});

        String gen = new String(Files.readAllBytes(out.resolve("src/n/N.cpp")), StandardCharsets.UTF_8);
        assertTrue("Number not mapped:\n" + gen, gen.contains("CppHelper::Number* x"));
        assertTrue("element not boxed:\n" + gen, gen.contains("CppHelper::boxNumber(*it)"));

        Path main = out.resolve("main.cpp");
        Files.write(main, MAIN.getBytes(StandardCharsets.UTF_8));
        Path exe = out.resolve("num_test");
        Process cp = new ProcessBuilder(compiler.toString(), "-std=c++17",
                "-I" + Path.of("cppRt").toAbsolutePath(), "-I" + out,
                out.resolve("src/n/N.cpp").toString(), main.toString(), "-o", exe.toString())
                .redirectErrorStream(true).start();
        String compileLog = new String(cp.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals("compile/link failed:\n" + compileLog, 0,
                cp.waitFor(60, TimeUnit.SECONDS) ? cp.exitValue() : -1);

        Process run = new ProcessBuilder(exe.toString()).redirectErrorStream(true).start();
        String log = new String(run.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals("run output:\n" + log, 0, run.waitFor(60, TimeUnit.SECONDS) ? run.exitValue() : -1);
    }
}
