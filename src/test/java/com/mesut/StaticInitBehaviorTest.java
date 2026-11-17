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

/** Verifies that a static initializer is emitted as si() and updates static state. */
public class StaticInitBehaviorTest {

    private static final String JAVA =
            "package s;\n" +
            "public class St {\n" +
            "    static int counter = 0;\n" +
            "    static { counter = 5; }\n" +
            "}\n";

    private static final String MAIN =
            "#include \"common.h\"\n" +
            "#include \"s/St.h\"\n" +
            "#include <cstdio>\n" +
            "int main(){ s::St::si(); printf(\"counter=%d\\n\", s::St::counter); return s::St::counter==5?0:1; }\n";

    @Test
    public void staticInitializerRuns() throws Exception {
        Path compiler = Arrays.stream(System.getenv("PATH").split(":"))
                .map(Path::of).map(d -> d.resolve("g++")).filter(Files::isExecutable)
                .findFirst().orElse(null);
        Assume.assumeTrue("g++ is not available", compiler != null);

        Path src = Files.createTempDirectory("j2cpp-si-src");
        Files.createDirectories(src.resolve("s"));
        Files.write(src.resolve("s/St.java"), JAVA.getBytes(StandardCharsets.UTF_8));
        Path out = Files.createTempDirectory("j2cpp-si-out");
        Cmd.main(new String[]{"-src", src.toString(), "-out", out.toString()});

        Path main = out.resolve("main.cpp");
        Files.write(main, MAIN.getBytes(StandardCharsets.UTF_8));
        Path exe = out.resolve("si_test");
        Process cp = new ProcessBuilder(compiler.toString(), "-std=c++17",
                "-I" + Path.of("cppRt").toAbsolutePath(), "-I" + out,
                out.resolve("src/s/St.cpp").toString(), main.toString(), "-o", exe.toString())
                .redirectErrorStream(true).start();
        String compileLog = new String(cp.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals("compile failed:\n" + compileLog, 0,
                cp.waitFor(60, TimeUnit.SECONDS) ? cp.exitValue() : -1);

        Process run = new ProcessBuilder(exe.toString()).redirectErrorStream(true).start();
        String log = new String(run.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals("run output:\n" + log, 0, run.waitFor(60, TimeUnit.SECONDS) ? run.exitValue() : -1);
    }
}
