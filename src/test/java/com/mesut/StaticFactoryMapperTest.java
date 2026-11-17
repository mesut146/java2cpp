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

/** Verifies List.of/Set.of/Arrays.asList map to C++ container builders, including varargs. */
public class StaticFactoryMapperTest {

    private static final String JAVA =
            "package fp;\n" +
            "import java.util.Arrays;\n" +
            "import java.util.List;\n" +
            "import java.util.Set;\n" +
            "public class F {\n" +
            "    public static int main(String[] a) {\n" +
            "        List<String> l = List.of(\"a\", \"b\");\n" +
            "        Set<String> s = Set.of(\"x\", \"y\");\n" +
            "        List<String> al = Arrays.asList(\"p\", \"q\", \"r\");\n" +
            "        return l.size() * 100 + s.size() * 10 + al.size();\n" +
            "    }\n" +
            "}\n";

    private static final String MAIN =
            "#include \"common.h\"\n" +
            "#include \"fp/F.h\"\n" +
            "#include <cstdio>\n" +
            "int main(){\n" +
            "    int r = fp::F::main(nullptr);\n" +
            "    printf(\"r=%d\\n\", r);\n" +
            "    return r == 223 ? 0 : 1;\n" +
            "}\n";

    @Test
    public void collectionFactoriesMapToCppContainers() throws Exception {
        Path compiler = Arrays.stream(System.getenv("PATH").split(":"))
                .map(Path::of).map(d -> d.resolve("g++")).filter(Files::isExecutable)
                .findFirst().orElse(null);
        Assume.assumeTrue("g++ is not available", compiler != null);

        Path src = Files.createTempDirectory("j2cpp-sf-src");
        Files.createDirectories(src.resolve("fp"));
        Files.write(src.resolve("fp/F.java"), JAVA.getBytes(StandardCharsets.UTF_8));
        Path out = Files.createTempDirectory("j2cpp-sf-out");
        Cmd.main(new String[]{"-src", src.toString(), "-out", out.toString()});

        String gen = new String(Files.readAllBytes(out.resolve("src/fp/F.cpp")), StandardCharsets.UTF_8);
        assertTrue("List.of not mapped:\n" + gen, gen.contains("CppHelper::from({"));
        assertTrue("Set.of not mapped:\n" + gen, gen.contains("CppHelper::fromSet({"));
        assertTrue("Arrays.asList not mapped:\n" + gen,
                count(gen, "CppHelper::from({") == 2);

        Path main = out.resolve("main.cpp");
        Files.write(main, MAIN.getBytes(StandardCharsets.UTF_8));
        Path exe = out.resolve("sf_test");
        Process cp = new ProcessBuilder(compiler.toString(), "-std=c++17",
                "-I" + Path.of("cppRt").toAbsolutePath(), "-I" + out,
                out.resolve("src/fp/F.cpp").toString(), main.toString(), "-o", exe.toString())
                .redirectErrorStream(true).start();
        String compileLog = new String(cp.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals("compile failed:\n" + compileLog, 0,
                cp.waitFor(60, TimeUnit.SECONDS) ? cp.exitValue() : -1);

        Process run = new ProcessBuilder(exe.toString()).redirectErrorStream(true).start();
        String log = new String(run.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals("run output:\n" + log, 0, run.waitFor(60, TimeUnit.SECONDS) ? run.exitValue() : -1);
    }

    private static int count(String text, String needle) {
        int n = 0, from = 0;
        while ((from = text.indexOf(needle, from)) >= 0) {
            n++;
            from += needle.length();
        }
        return n;
    }
}
