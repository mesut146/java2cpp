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

/** Verifies enum constants are defined/initialized and expose ordinal/name/toString/compareTo. */
public class EnumConstantsTest {

    private static final String JAVA =
            "package ep;\n" +
            "public class E {\n" +
            "    public enum Color {\n" +
            "        RED(10), GREEN(20), BLUE(30);\n" +
            "        int code;\n" +
            "        Color(int code) { this.code = code; }\n" +
            "    }\n" +
            "    public static int main(String[] a) {\n" +
            "        Color c = Color.GREEN;\n" +
            "        return c.ordinal() * 100 + c.code\n" +
            "             + (c.name().equals(\"GREEN\") ? 1000 : 0)\n" +
            "             + (c.toString().equals(\"GREEN\") ? 10000 : 0)\n" +
            "             + (c.compareTo(Color.BLUE) < 0 ? 100000 : 0);\n" +
            "    }\n" +
            "}\n";

    private static final String MAIN =
            "#include \"common.h\"\n" +
            "#include \"ep/E.h\"\n" +
            "#include <cstdio>\n" +
            "int main(){\n" +
            "    int r = ep::E::main(nullptr);\n" +
            "    printf(\"r=%d\\n\", r);\n" +
            "    return r == 111120 ? 0 : 1;\n" +
            "}\n";

    @Test
    public void enumConstantsAreInitialized() throws Exception {
        Path compiler = Arrays.stream(System.getenv("PATH").split(":"))
                .map(Path::of).map(d -> d.resolve("g++")).filter(Files::isExecutable)
                .findFirst().orElse(null);
        Assume.assumeTrue("g++ is not available", compiler != null);

        Path src = Files.createTempDirectory("j2cpp-en-src");
        Files.createDirectories(src.resolve("ep"));
        Files.write(src.resolve("ep/E.java"), JAVA.getBytes(StandardCharsets.UTF_8));
        Path out = Files.createTempDirectory("j2cpp-en-out");
        Cmd.main(new String[]{"-src", src.toString(), "-out", out.toString()});

        String gen = new String(Files.readAllBytes(out.resolve("src/ep/E.cpp")), StandardCharsets.UTF_8);
        assertTrue("constants not defined:\n" + gen, gen.contains("E_Color* E_Color::GREEN = nullptr;"));
        assertTrue("constants not initialized:\n" + gen, gen.contains("new E_Color(20)"));
        String header = new String(Files.readAllBytes(out.resolve("ep/E_Color.h")), StandardCharsets.UTF_8);
        assertTrue("ordinal missing:\n" + header, header.contains("int ordinal()"));

        Path main = out.resolve("main.cpp");
        Files.write(main, MAIN.getBytes(StandardCharsets.UTF_8));
        Path exe = out.resolve("en_test");
        Process cp = new ProcessBuilder(compiler.toString(), "-std=c++17",
                "-I" + Path.of("cppRt").toAbsolutePath(), "-I" + out,
                out.resolve("src/ep/E.cpp").toString(), main.toString(), "-o", exe.toString())
                .redirectErrorStream(true).start();
        String compileLog = new String(cp.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals("compile failed:\n" + compileLog, 0,
                cp.waitFor(60, TimeUnit.SECONDS) ? cp.exitValue() : -1);

        Process run = new ProcessBuilder(exe.toString()).redirectErrorStream(true).start();
        String log = new String(run.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals("run output:\n" + log, 0, run.waitFor(60, TimeUnit.SECONDS) ? run.exitValue() : -1);
    }
}
