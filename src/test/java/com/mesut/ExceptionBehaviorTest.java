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

/** Verifies multi-catch and rethrow behavior of generated exception handling. */
public class ExceptionBehaviorTest {

    private static final String JAVA =
            "package e;\n" +
            "public class Ex {\n" +
            "    static int log = 0;\n" +
            "    static int test(int mode) {\n" +
            "        try {\n" +
            "            if (mode == 1) throw new IllegalStateException(\"a\");\n" +
            "            return 0;\n" +
            "        } catch (IllegalStateException | IllegalArgumentException ex) {\n" +
            "            log = 1;\n" +
            "            return 10;\n" +
            "        }\n" +
            "    }\n" +
            "    static int rethrow() {\n" +
            "        try { throw new IllegalStateException(\"x\"); }\n" +
            "        catch (IllegalStateException ex) { log = 2; throw ex; }\n" +
            "    }\n" +
            "}\n";

    private static final String MAIN =
            "#include \"common.h\"\n" +
            "#include \"e/Ex.h\"\n" +
            "#include \"lib/java/io/Serializable.h\"\n" +
            "#include \"lib/java/lang/Throwable.h\"\n" +
            "#include \"lib/java/lang/Exception.h\"\n" +
            "#include \"lib/java/lang/RuntimeException.h\"\n" +
            "#include \"lib/java/lang/IllegalStateException.h\"\n" +
            "#include <cstdio>\n" +
            "namespace java { namespace lang {\n" +
            "IllegalStateException::IllegalStateException(std::string* s) {}\n" +
            "}}\n" +
            "int main(){\n" +
            "    e::Ex::log=0; int a=e::Ex::test(0);\n" +
            "    e::Ex::log=0; int b=e::Ex::test(1);\n" +
            "    bool caught=false; try { e::Ex::rethrow(); } catch (java::lang::IllegalStateException&) { caught=true; }\n" +
            "    printf(\"a=%d b=%d log=%d caught=%d\\n\", a, b, e::Ex::log, caught);\n" +
            "    return (a==0 && b==10 && e::Ex::log==2 && caught) ? 0 : 1;\n" +
            "}\n";

    @Test
    public void multiCatchAndRethrow() throws Exception {
        Path compiler = Arrays.stream(System.getenv("PATH").split(":"))
                .map(Path::of).map(d -> d.resolve("g++")).filter(Files::isExecutable)
                .findFirst().orElse(null);
        Assume.assumeTrue("g++ is not available", compiler != null);

        Path src = Files.createTempDirectory("j2cpp-ex-src");
        Files.createDirectories(src.resolve("e"));
        Files.write(src.resolve("e/Ex.java"), JAVA.getBytes(StandardCharsets.UTF_8));
        Path out = Files.createTempDirectory("j2cpp-ex-out");
        Cmd.main(new String[]{"-src", src.toString(), "-out", out.toString()});

        Path main = out.resolve("main.cpp");
        Files.write(main, MAIN.getBytes(StandardCharsets.UTF_8));
        Path exe = out.resolve("ex_test");
        Process cp = new ProcessBuilder(compiler.toString(), "-std=c++17",
                "-I" + Path.of("cppRt").toAbsolutePath(), "-I" + out,
                out.resolve("src/e/Ex.cpp").toString(), main.toString(), "-o", exe.toString())
                .redirectErrorStream(true).start();
        String compileLog = new String(cp.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals("compile failed:\n" + compileLog, 0,
                cp.waitFor(60, TimeUnit.SECONDS) ? cp.exitValue() : -1);

        Process run = new ProcessBuilder(exe.toString()).redirectErrorStream(true).start();
        String log = new String(run.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals("run output:\n" + log, 0, run.waitFor(60, TimeUnit.SECONDS) ? run.exitValue() : -1);
    }
}
