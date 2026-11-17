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

/** Verifies enhanced-for over an expression evaluates it once and iterates a mapped collection. */
public class EnhancedForTest {

    private static final String JAVA =
            "package ef;\n" +
            "import java.util.ArrayList;\n" +
            "import java.util.List;\n" +
            "public class E {\n" +
            "    static int calls = 0;\n" +
            "    static List<String> getList() {\n" +
            "        calls++;\n" +
            "        List<String> l = new ArrayList<String>();\n" +
            "        l.add(\"a\");\n" +
            "        l.add(\"b\");\n" +
            "        return l;\n" +
            "    }\n" +
            "    public static int main(String[] args) {\n" +
            "        int count = 0;\n" +
            "        for (String s : getList()) { count = count + 1; }\n" +
            "        return calls * 10 + count;\n" +
            "    }\n" +
            "}\n";

    private static final String MAIN =
            "#include \"common.h\"\n" +
            "#include \"ef/E.h\"\n" +
            "#include <cstdio>\n" +
            "int main(){\n" +
            "    int r = ef::E::main(nullptr);\n" +
            "    printf(\"r=%d\\n\", r);\n" +
            "    return r == 12 ? 0 : 1;\n" +
            "}\n";

    @Test
    public void enhancedForEvaluatesExpressionOnce() throws Exception {
        Path compiler = Arrays.stream(System.getenv("PATH").split(":"))
                .map(Path::of).map(d -> d.resolve("g++")).filter(Files::isExecutable)
                .findFirst().orElse(null);
        Assume.assumeTrue("g++ is not available", compiler != null);

        Path src = Files.createTempDirectory("j2cpp-ef-src");
        Files.createDirectories(src.resolve("ef"));
        Files.write(src.resolve("ef/E.java"), JAVA.getBytes(StandardCharsets.UTF_8));
        Path out = Files.createTempDirectory("j2cpp-ef-out");
        Cmd.main(new String[]{"-src", src.toString(), "-out", out.toString()});

        String gen = new String(Files.readAllBytes(out.resolve("src/ef/E.cpp")), StandardCharsets.UTF_8);
        assertTrue("expression re-evaluated in loop:\n" + gen,
                countOccurrences(gen, "E::getList();") == 1);

        Path main = out.resolve("main.cpp");
        Files.write(main, MAIN.getBytes(StandardCharsets.UTF_8));
        Path exe = out.resolve("ef_test");
        Process cp = new ProcessBuilder(compiler.toString(), "-std=c++17",
                "-I" + Path.of("cppRt").toAbsolutePath(), "-I" + out,
                out.resolve("src/ef/E.cpp").toString(), main.toString(), "-o", exe.toString())
                .redirectErrorStream(true).start();
        String compileLog = new String(cp.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals("compile failed:\n" + compileLog, 0,
                cp.waitFor(60, TimeUnit.SECONDS) ? cp.exitValue() : -1);

        Process run = new ProcessBuilder(exe.toString()).redirectErrorStream(true).start();
        String log = new String(run.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals("run output:\n" + log, 0, run.waitFor(60, TimeUnit.SECONDS) ? run.exitValue() : -1);
    }

    private static int countOccurrences(String text, String needle) {
        int count = 0, from = 0;
        while ((from = text.indexOf(needle, from)) >= 0) {
            count++;
            from += needle.length();
        }
        return count;
    }
}
