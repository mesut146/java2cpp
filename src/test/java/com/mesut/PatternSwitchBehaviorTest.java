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

/** Verifies that a switch over sealed records with type patterns dispatches correctly. */
public class PatternSwitchBehaviorTest {

    private static final String JAVA =
            "package m;\n" +
            "public class Modern {\n" +
            "    sealed interface Shape permits Circle, Square {}\n" +
            "    record Circle(double r) implements Shape {}\n" +
            "    record Square(double s) implements Shape {}\n" +
            "    static int kind(Shape shape) {\n" +
            "        return switch (shape) {\n" +
            "            case Circle c -> 1;\n" +
            "            case Square q -> 2;\n" +
            "        };\n" +
            "    }\n" +
            "}\n";

    private static final String MAIN =
            "#include \"common.h\"\n" +
            "#include \"m/Modern.h\"\n" +
            "#include \"m/Modern_Circle.h\"\n" +
            "#include \"m/Modern_Square.h\"\n" +
            "#include <cstdio>\n" +
            "int main(){\n" +
            "    m::Modern_Circle c(2.0); m::Modern_Square s(3.0);\n" +
            "    int a=m::Modern::kind(&c), b=m::Modern::kind(&s);\n" +
            "    printf(\"a=%d b=%d\\n\", a, b);\n" +
            "    return (a==1 && b==2) ? 0 : 1;\n" +
            "}\n";

    @Test
    public void patternSwitchDispatches() throws Exception {
        Path compiler = Arrays.stream(System.getenv("PATH").split(":"))
                .map(Path::of).map(d -> d.resolve("g++")).filter(Files::isExecutable)
                .findFirst().orElse(null);
        Assume.assumeTrue("g++ is not available", compiler != null);

        Path src = Files.createTempDirectory("j2cpp-ps-src");
        Files.createDirectories(src.resolve("m"));
        Files.write(src.resolve("m/Modern.java"), JAVA.getBytes(StandardCharsets.UTF_8));
        Path out = Files.createTempDirectory("j2cpp-ps-out");
        Cmd.main(new String[]{"-src", src.toString(), "-out", out.toString()});

        Path main = out.resolve("main.cpp");
        Files.write(main, MAIN.getBytes(StandardCharsets.UTF_8));
        Path exe = out.resolve("ps_test");
        Process cp = new ProcessBuilder(compiler.toString(), "-std=c++17",
                "-I" + Path.of("cppRt").toAbsolutePath(), "-I" + out,
                out.resolve("src/m/Modern.cpp").toString(), main.toString(), "-o", exe.toString())
                .redirectErrorStream(true).start();
        String compileLog = new String(cp.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals("compile failed:\n" + compileLog, 0,
                cp.waitFor(60, TimeUnit.SECONDS) ? cp.exitValue() : -1);

        Process run = new ProcessBuilder(exe.toString()).redirectErrorStream(true).start();
        String log = new String(run.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals("run output:\n" + log, 0, run.waitFor(60, TimeUnit.SECONDS) ? run.exitValue() : -1);
    }
}
