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

/**
 * Verifies that generated try/finally has Java semantics by compiling and running
 * the generated C++: finally runs on normal completion, early return and throw,
 * and a return in finally overrides the try result or a pending exception.
 */
public class TryFinallyBehaviorTest {

    private static final String JAVA =
            "package t;\n" +
            "public class F {\n" +
            "    static int log = 0;\n" +
            "    int normal() { try { log = log * 10 + 1; } finally { log = log * 10 + 9; } return log; }\n" +
            "    int earlyReturn() { try { log = log * 10 + 1; return 5; } finally { log = log * 10 + 9; } }\n" +
            "    int finallyWins() { try { log = log * 10 + 1; return 1; } finally { log = log * 10 + 9; return 2; } }\n" +
            "    int throwing() { try { log = log * 10 + 1; throw new RuntimeException(\"x\"); } finally { log = log * 10 + 9; } }\n" +
            "    int throwFinallyWins() { try { log = log * 10 + 1; throw new RuntimeException(\"x\"); } finally { log = log * 10 + 9; return 7; } }\n" +
            "}\n";

    private static final String MAIN =
            "#include \"common.h\"\n" +
            "#include \"t/F.h\"\n" +
            "#include <cstdio>\n" +
            "int main() {\n" +
            "    t::F f; int fails = 0;\n" +
            "    t::F::log=0; int a=f.normal();\n" +
            "    if(a!=19||t::F::log!=19) { printf(\"normal=%d log=%d\\n\", a, t::F::log); fails++; }\n" +
            "    t::F::log=0; int b=f.earlyReturn();\n" +
            "    if(b!=5||t::F::log!=19) { printf(\"early=%d log=%d\\n\", b, t::F::log); fails++; }\n" +
            "    t::F::log=0; int c=f.finallyWins();\n" +
            "    if(c!=2||t::F::log!=19) { printf(\"wins=%d log=%d\\n\", c, t::F::log); fails++; }\n" +
            "    t::F::log=0; bool caught=false;\n" +
            "    try { f.throwing(); } catch (CppHelper::Throwable&) { caught=true; }\n" +
            "    if(!caught||t::F::log!=19) { printf(\"throwing caught=%d log=%d\\n\", caught, t::F::log); fails++; }\n" +
            "    t::F::log=0; int d=f.throwFinallyWins();\n" +
            "    if(d!=7||t::F::log!=19) { printf(\"throwWins=%d log=%d\\n\", d, t::F::log); fails++; }\n" +
            "    printf(\"fails=%d\\n\", fails);\n" +
            "    return fails;\n" +
            "}\n";

    @Test
    public void finallySemantics() throws Exception {
        Path compiler = Arrays.stream(System.getenv("PATH").split(":"))
                .map(Path::of).map(d -> d.resolve("g++")).filter(Files::isExecutable)
                .findFirst().orElse(null);
        Assume.assumeTrue("g++ is not available", compiler != null);

        Path src = Files.createTempDirectory("j2cpp-fin-src");
        Files.createDirectories(src.resolve("t"));
        Files.write(src.resolve("t/F.java"), JAVA.getBytes(StandardCharsets.UTF_8));
        Path out = Files.createTempDirectory("j2cpp-fin-out");
        Cmd.main(new String[]{"-src", src.toString(), "-out", out.toString()});

        Path main = out.resolve("main.cpp");
        Files.write(main, MAIN.getBytes(StandardCharsets.UTF_8));
        Path exe = out.resolve("fin_test");
        ProcessBuilder compile = new ProcessBuilder(compiler.toString(), "-std=c++17",
                "-I" + Path.of("cppRt").toAbsolutePath(), "-I" + out,
                out.resolve("src/t/F.cpp").toString(), main.toString(), "-o", exe.toString())
                .redirectErrorStream(true);
        Process compileProcess = compile.start();
        String compileLog = new String(compileProcess.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        int compileExit = compileProcess.waitFor(60, TimeUnit.SECONDS) ? compileProcess.exitValue() : -1;
        assertEquals("compile failed:\n" + compileLog, 0, compileExit);

        Process run = new ProcessBuilder(exe.toString()).redirectErrorStream(true).start();
        String log = new String(run.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        int exit = run.waitFor(60, TimeUnit.SECONDS) ? run.exitValue() : -1;
        assertEquals("compile failed:\n" + compileLog + "\nrun output:\n" + log, 0, exit);
    }
}
