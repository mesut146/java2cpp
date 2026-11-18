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

/** Implicit Java boxing/unboxing in Object-typed positions. */
public class BoxingTest {

    private static final String JAVA = """
            package b;
            public class B {
                public static int main(String[] a) {
                    Object o = 5;
                    Integer i = 7;
                    Object fromVar = i;
                    Number n = i;
                    Object t = true;
                    int x = i;
                    return (Integer) o + x + (Integer) fromVar + n.intValue()
                            + (((Boolean) t) ? 1 : 0);
                }
            }
            """;

    private static final String MAIN = """
            #include "common.h"
            #include "b/B.h"
            #include <cstdio>
            int main(){
                int r = b::B::main(nullptr);
                printf("r=%d\\n", r);
                return r == 27 ? 0 : 1;
            }
            """;

    @Test
    public void primitivesBoxToObjectAndCastBack() throws Exception {
        Path compiler = Arrays.stream(System.getenv("PATH").split(":"))
                .map(Path::of).map(d -> d.resolve("g++")).filter(Files::isExecutable)
                .findFirst().orElse(null);
        Assume.assumeTrue("g++ is not available", compiler != null);

        Path src = Files.createTempDirectory("j2cpp-box-src");
        Files.createDirectories(src.resolve("b"));
        Files.write(src.resolve("b/B.java"), JAVA.getBytes(StandardCharsets.UTF_8));
        Path out = Files.createTempDirectory("j2cpp-box-out");
        Cmd.main(new String[]{"-src", src.toString(), "-out", out.toString()});

        String cpp = new String(Files.readAllBytes(out.resolve("src/b/B.cpp")), StandardCharsets.UTF_8);
        assertTrue("primitive not boxed:\n" + cpp, cpp.contains("new j2cpp::Box<int>(5)"));
        assertTrue("mapped wrapper variable not boxed:\n" + cpp,
                cpp.contains("new j2cpp::Box<int>(i)"));
        assertTrue("boolean not boxed:\n" + cpp, cpp.contains("new j2cpp::Box<bool>(true)"));
        assertTrue("cast not unboxed:\n" + cpp, cpp.contains("j2cpp::unbox<int>(o)"));
        assertTrue("Number boxing missing:\n" + cpp, cpp.contains("new CppHelper::Number(i)"));

        String log = new String(Files.readAllBytes(out.resolve("log.txt")), StandardCharsets.UTF_8);
        assertTrue("boxing not logged:\n" + log, log.contains("primitive boxed for Object-typed target"));
        assertTrue("unboxing not logged:\n" + log, log.contains("cast to boxed primitive unboxed"));

        Path main = out.resolve("main.cpp");
        Files.write(main, MAIN.getBytes(StandardCharsets.UTF_8));
        Path exe = out.resolve("box_test");
        Process cp = new ProcessBuilder(compiler.toString(), "-std=c++17",
                "-I" + Path.of("cppRt").toAbsolutePath(), "-I" + out,
                out.resolve("src/b/B.cpp").toString(), main.toString(), "-o", exe.toString())
                .redirectErrorStream(true).start();
        String compileLog = new String(cp.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals("compile/link failed:\n" + compileLog, 0,
                cp.waitFor(60, TimeUnit.SECONDS) ? cp.exitValue() : -1);

        Process run = new ProcessBuilder(exe.toString()).redirectErrorStream(true).start();
        String runLog = new String(run.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals("run output:\n" + runLog, 0, run.waitFor(60, TimeUnit.SECONDS) ? run.exitValue() : -1);
    }
}
