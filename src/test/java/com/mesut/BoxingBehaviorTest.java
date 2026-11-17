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

/** Verifies boxed Integer values, List<Integer> and int[] arrays at runtime. */
public class BoxingBehaviorTest {

    private static final String JAVA =
            "package b;\n" +
            "import java.util.List;\n" +
            "import java.util.ArrayList;\n" +
            "public class Box {\n" +
            "    public int test() {\n" +
            "        Integer boxed = Integer.valueOf(7);\n" +
            "        int unboxed = boxed;\n" +
            "        List<Integer> xs = new ArrayList<>();\n" +
            "        xs.add(3);\n" +
            "        xs.add(5);\n" +
            "        int sum = xs.get(0) + xs.get(1);\n" +
            "        int[] arr = new int[3];\n" +
            "        arr[0] = 10; arr[1] = 20; arr[2] = 30;\n" +
            "        return unboxed * 1000 + sum * 100 + arr[1] + arr.length;\n" +
            "    }\n" +
            "}\n";

    private static final String MAIN =
            "#include \"common.h\"\n" +
            "#include \"b/Box.h\"\n" +
            "#include <cstdio>\n" +
            "int main(){ b::Box o; int r=o.test(); printf(\"r=%d\\n\", r); return r==7823?0:1; }\n";

    @Test
    public void boxingAndArraysBehave() throws Exception {
        Path compiler = Arrays.stream(System.getenv("PATH").split(":"))
                .map(Path::of).map(d -> d.resolve("g++")).filter(Files::isExecutable)
                .findFirst().orElse(null);
        Assume.assumeTrue("g++ is not available", compiler != null);

        Path src = Files.createTempDirectory("j2cpp-box-src");
        Files.createDirectories(src.resolve("b"));
        Files.write(src.resolve("b/Box.java"), JAVA.getBytes(StandardCharsets.UTF_8));
        Path out = Files.createTempDirectory("j2cpp-box-out");
        Cmd.main(new String[]{"-src", src.toString(), "-out", out.toString()});

        Path main = out.resolve("main.cpp");
        Files.write(main, MAIN.getBytes(StandardCharsets.UTF_8));
        Path exe = out.resolve("box_test");
        Process cp = new ProcessBuilder(compiler.toString(), "-std=c++17",
                "-I" + Path.of("cppRt").toAbsolutePath(), "-I" + out,
                out.resolve("src/b/Box.cpp").toString(), main.toString(), "-o", exe.toString())
                .redirectErrorStream(true).start();
        String compileLog = new String(cp.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals("compile failed:\n" + compileLog, 0,
                cp.waitFor(60, TimeUnit.SECONDS) ? cp.exitValue() : -1);

        Process run = new ProcessBuilder(exe.toString()).redirectErrorStream(true).start();
        String log = new String(run.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals("run output:\n" + log, 0, run.waitFor(60, TimeUnit.SECONDS) ? run.exitValue() : -1);
    }
}
