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

/** Verifies that a switch expression is translated to a value-producing lambda. */
public class SwitchExpressionBehaviorTest {

    private static final String JAVA =
            "package s;\n" +
            "public class SE {\n" +
            "    int classify(int x) {\n" +
            "        int result = switch (x) {\n" +
            "            case 1 -> 10;\n" +
            "            case 2 -> 20;\n" +
            "            default -> 30;\n" +
            "        };\n" +
            "        return result;\n" +
            "    }\n" +
            "}\n";

    private static final String MAIN =
            "#include \"common.h\"\n" +
            "#include \"s/SE.h\"\n" +
            "#include <cstdio>\n" +
            "int main(){\n" +
            "    s::SE o;\n" +
            "    int a=o.classify(1), b=o.classify(2), c=o.classify(9);\n" +
            "    printf(\"a=%d b=%d c=%d\\n\", a, b, c);\n" +
            "    return (a==10 && b==20 && c==30) ? 0 : 1;\n" +
            "}\n";

    @Test
    public void switchExpressionProducesValue() throws Exception {
        Path compiler = Arrays.stream(System.getenv("PATH").split(":"))
                .map(Path::of).map(d -> d.resolve("g++")).filter(Files::isExecutable)
                .findFirst().orElse(null);
        Assume.assumeTrue("g++ is not available", compiler != null);

        Path src = Files.createTempDirectory("j2cpp-se-src");
        Files.createDirectories(src.resolve("s"));
        Files.write(src.resolve("s/SE.java"), JAVA.getBytes(StandardCharsets.UTF_8));
        Path out = Files.createTempDirectory("j2cpp-se-out");
        Cmd.main(new String[]{"-src", src.toString(), "-out", out.toString()});

        String cpp = new String(Files.readAllBytes(out.resolve("src/s/SE.cpp")), StandardCharsets.UTF_8);
        assertEquals("lambda not emitted: " + cpp, 1, cpp.indexOf("[&]() -> int") >= 0 ? 1 : 0);

        Path main = out.resolve("main.cpp");
        Files.write(main, MAIN.getBytes(StandardCharsets.UTF_8));
        Path exe = out.resolve("se_test");
        Process cp = new ProcessBuilder(compiler.toString(), "-std=c++17",
                "-I" + Path.of("cppRt").toAbsolutePath(), "-I" + out,
                out.resolve("src/s/SE.cpp").toString(), main.toString(), "-o", exe.toString())
                .redirectErrorStream(true).start();
        String compileLog = new String(cp.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals("compile failed:\n" + compileLog, 0,
                cp.waitFor(60, TimeUnit.SECONDS) ? cp.exitValue() : -1);

        Process run = new ProcessBuilder(exe.toString()).redirectErrorStream(true).start();
        String log = new String(run.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals("run output:\n" + log, 0, run.waitFor(60, TimeUnit.SECONDS) ? run.exitValue() : -1);
    }
}
