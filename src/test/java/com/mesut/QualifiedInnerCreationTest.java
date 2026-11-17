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

/** Verifies q.new Inner() passes q as the enclosing instance, even in a static method. */
public class QualifiedInnerCreationTest {

    private static final String JAVA =
            "package qt;\n" +
            "public class Q {\n" +
            "    int x;\n" +
            "    Q(int x) { this.x = x; }\n" +
            "    class Inner {\n" +
            "        int get() { return Q.this.x; }\n" +
            "    }\n" +
            "    public static int main(String[] a) {\n" +
            "        Q q = new Q(5);\n" +
            "        return q.new Inner().get();\n" +
            "    }\n" +
            "}\n";

    private static final String MAIN =
            "#include \"common.h\"\n" +
            "#include \"qt/Q.h\"\n" +
            "#include <cstdio>\n" +
            "int main(){\n" +
            "    int r = qt::Q::main(nullptr);\n" +
            "    printf(\"r=%d\\n\", r);\n" +
            "    return r == 5 ? 0 : 1;\n" +
            "}\n";

    @Test
    public void qualifiedInnerUsesEnclosingInstance() throws Exception {
        Path compiler = Arrays.stream(System.getenv("PATH").split(":"))
                .map(Path::of).map(d -> d.resolve("g++")).filter(Files::isExecutable)
                .findFirst().orElse(null);
        Assume.assumeTrue("g++ is not available", compiler != null);

        Path src = Files.createTempDirectory("j2cpp-qi-src");
        Files.createDirectories(src.resolve("qt"));
        Files.write(src.resolve("qt/Q.java"), JAVA.getBytes(StandardCharsets.UTF_8));
        Path out = Files.createTempDirectory("j2cpp-qi-out");
        Cmd.main(new String[]{"-src", src.toString(), "-out", out.toString()});

        String gen = new String(Files.readAllBytes(out.resolve("src/qt/Q.cpp")), StandardCharsets.UTF_8);
        assertTrue("wrong enclosing instance:\n" + gen, gen.contains("new Q_Inner(q)"));
        assertTrue("receiver needs parentheses:\n" + gen, gen.contains("(new Q_Inner(q))->get()"));

        Path main = out.resolve("main.cpp");
        Files.write(main, MAIN.getBytes(StandardCharsets.UTF_8));
        Path exe = out.resolve("qi_test");
        Process cp = new ProcessBuilder(compiler.toString(), "-std=c++17",
                "-I" + Path.of("cppRt").toAbsolutePath(), "-I" + out,
                out.resolve("src/qt/Q.cpp").toString(), main.toString(), "-o", exe.toString())
                .redirectErrorStream(true).start();
        String compileLog = new String(cp.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals("compile failed:\n" + compileLog, 0,
                cp.waitFor(60, TimeUnit.SECONDS) ? cp.exitValue() : -1);

        Process run = new ProcessBuilder(exe.toString()).redirectErrorStream(true).start();
        String log = new String(run.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals("run output:\n" + log, 0, run.waitFor(60, TimeUnit.SECONDS) ? run.exitValue() : -1);
    }
}
