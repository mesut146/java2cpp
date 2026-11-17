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

/** Verifies that an inner class can read and write an enclosing field at runtime. */
public class InnerClassBehaviorTest {

    private static final String JAVA =
            "package i;\n" +
            "public class Inner {\n" +
            "    int value = 10;\n" +
            "    class Child {\n" +
            "        int read() { return value; }\n" +
            "        void write(int v) { value = v; }\n" +
            "    }\n" +
            "    int test() {\n" +
            "        Child c = new Child();\n" +
            "        int a = c.read();\n" +
            "        c.write(99);\n" +
            "        return a * 100 + value;\n" +
            "    }\n" +
            "}\n";

    private static final String MAIN =
            "#include \"common.h\"\n" +
            "#include \"i/Inner.h\"\n" +
            "#include <cstdio>\n" +
            "int main(){\n" +
            "    i::Inner o; o.value = 10;\n" +
            "    int r = o.test();\n" +
            "    printf(\"r=%d value=%d\\n\", r, o.value);\n" +
            "    return (r == 1099 && o.value == 99) ? 0 : 1;\n" +
            "}\n";

    @Test
    public void innerClassAccessesOuterField() throws Exception {
        Path compiler = Arrays.stream(System.getenv("PATH").split(":"))
                .map(Path::of).map(d -> d.resolve("g++")).filter(Files::isExecutable)
                .findFirst().orElse(null);
        Assume.assumeTrue("g++ is not available", compiler != null);

        Path src = Files.createTempDirectory("j2cpp-inner-src");
        Files.createDirectories(src.resolve("i"));
        Files.write(src.resolve("i/Inner.java"), JAVA.getBytes(StandardCharsets.UTF_8));
        Path out = Files.createTempDirectory("j2cpp-inner-out");
        Cmd.main(new String[]{"-src", src.toString(), "-out", out.toString()});

        Path main = out.resolve("main.cpp");
        Files.write(main, MAIN.getBytes(StandardCharsets.UTF_8));
        Path exe = out.resolve("inner_test");
        Process cp = new ProcessBuilder(compiler.toString(), "-std=c++17",
                "-I" + Path.of("cppRt").toAbsolutePath(), "-I" + out,
                out.resolve("src/i/Inner.cpp").toString(), main.toString(), "-o", exe.toString())
                .redirectErrorStream(true).start();
        String compileLog = new String(cp.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals("compile failed:\n" + compileLog, 0,
                cp.waitFor(60, TimeUnit.SECONDS) ? cp.exitValue() : -1);

        Process run = new ProcessBuilder(exe.toString()).redirectErrorStream(true).start();
        String log = new String(run.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals("run output:\n" + log, 0, run.waitFor(60, TimeUnit.SECONDS) ? run.exitValue() : -1);
    }
}
