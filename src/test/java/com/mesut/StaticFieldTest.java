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

/** Verifies static field writes, String += and defined cofui accessors for static finals. */
public class StaticFieldTest {

    private static final String JAVA =
            "package sa;\n" +
            "public class S {\n" +
            "    static String seen = \"\";\n" +
            "    static final String TAG = \"tag\";\n" +
            "    static int count = 0;\n" +
            "    public static int main(String[] a) {\n" +
            "        seen += \"x\";\n" +
            "        count += 2;\n" +
            "        String t = TAG;\n" +
            "        return count + seen.length() + t.length();\n" +
            "    }\n" +
            "}\n";

    private static final String MAIN =
            "#include \"common.h\"\n" +
            "#include \"sa/S.h\"\n" +
            "#include <cstdio>\n" +
            "int main(){\n" +
            "    int r = sa::S::main(nullptr);\n" +
            "    printf(\"r=%d\\n\", r);\n" +
            "    return r == 6 ? 0 : 1;\n" +
            "}\n";

    @Test
    public void staticFieldsAndStringPlusEquals() throws Exception {
        Path compiler = Arrays.stream(System.getenv("PATH").split(":"))
                .map(Path::of).map(d -> d.resolve("g++")).filter(Files::isExecutable)
                .findFirst().orElse(null);
        Assume.assumeTrue("g++ is not available", compiler != null);

        Path src = Files.createTempDirectory("j2cpp-sf2-src");
        Files.createDirectories(src.resolve("sa"));
        Files.write(src.resolve("sa/S.java"), JAVA.getBytes(StandardCharsets.UTF_8));
        Path out = Files.createTempDirectory("j2cpp-sf2-out");
        Cmd.main(new String[]{"-src", src.toString(), "-out", out.toString()});

        String gen = new String(Files.readAllBytes(out.resolve("src/sa/S.cpp")), StandardCharsets.UTF_8);
        assertTrue("static field emitted as accessor:\n" + gen, gen.contains("S::seen = "));
        assertTrue("String += not concatenated:\n" + gen, gen.contains("CppHelper::concat(S::seen"));
        assertTrue("cofui accessor not defined:\n" + gen, gen.contains("std::string* S::TAG()"));

        Path main = out.resolve("main.cpp");
        Files.write(main, MAIN.getBytes(StandardCharsets.UTF_8));
        Path exe = out.resolve("sf2_test");
        Process cp = new ProcessBuilder(compiler.toString(), "-std=c++17",
                "-I" + Path.of("cppRt").toAbsolutePath(), "-I" + out,
                out.resolve("src/sa/S.cpp").toString(), main.toString(), "-o", exe.toString())
                .redirectErrorStream(true).start();
        String compileLog = new String(cp.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals("compile failed:\n" + compileLog, 0,
                cp.waitFor(60, TimeUnit.SECONDS) ? cp.exitValue() : -1);

        Process run = new ProcessBuilder(exe.toString()).redirectErrorStream(true).start();
        String log = new String(run.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals("run output:\n" + log, 0, run.waitFor(60, TimeUnit.SECONDS) ? run.exitValue() : -1);
    }
}
