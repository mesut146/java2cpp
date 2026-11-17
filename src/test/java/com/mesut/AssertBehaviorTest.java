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

/** Verifies that Java assert is emitted as a C++ assert with the right include. */
public class AssertBehaviorTest {

    private static final String JAVA =
            "package a;\n" +
            "public class A {\n" +
            "    int test(int x) {\n" +
            "        assert x > 0;\n" +
            "        return x;\n" +
            "    }\n" +
            "}\n";

    private static final String MAIN =
            "#include \"common.h\"\n" +
            "#include \"a/A.h\"\n" +
            "#include <cstdio>\n" +
            "int main(){ a::A o; printf(\"r=%d\\n\", o.test(5)); return o.test(5)==5?0:1; }\n";

    @Test
    public void assertIsEmitted() throws Exception {
        Path compiler = Arrays.stream(System.getenv("PATH").split(":"))
                .map(Path::of).map(d -> d.resolve("g++")).filter(Files::isExecutable)
                .findFirst().orElse(null);
        Assume.assumeTrue("g++ is not available", compiler != null);

        Path src = Files.createTempDirectory("j2cpp-assert-src");
        Files.createDirectories(src.resolve("a"));
        Files.write(src.resolve("a/A.java"), JAVA.getBytes(StandardCharsets.UTF_8));
        Path out = Files.createTempDirectory("j2cpp-assert-out");
        Cmd.main(new String[]{"-src", src.toString(), "-out", out.toString()});

        String cpp = new String(Files.readAllBytes(out.resolve("src/a/A.cpp")), StandardCharsets.UTF_8);
        assertTrue("assert not emitted: " + cpp, cpp.contains("assert(x>0)"));

        Path main = out.resolve("main.cpp");
        Files.write(main, MAIN.getBytes(StandardCharsets.UTF_8));
        Path exe = out.resolve("assert_test");
        Process cp = new ProcessBuilder(compiler.toString(), "-std=c++17",
                "-I" + Path.of("cppRt").toAbsolutePath(), "-I" + out,
                out.resolve("src/a/A.cpp").toString(), main.toString(), "-o", exe.toString())
                .redirectErrorStream(true).start();
        String compileLog = new String(cp.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals("compile failed:\n" + compileLog, 0,
                cp.waitFor(60, TimeUnit.SECONDS) ? cp.exitValue() : -1);

        Process run = new ProcessBuilder(exe.toString()).redirectErrorStream(true).start();
        String log = new String(run.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals("run output:\n" + log, 0, run.waitFor(60, TimeUnit.SECONDS) ? run.exitValue() : -1);
    }
}
