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

/** Verifies that an override of a method declared several levels up dispatches virtually. */
public class VirtualDispatchTest {

    private static final String JAVA =
            "package vp;\n" +
            "public class V {\n" +
            "    public static class A { public String who() { return \"A\"; } }\n" +
            "    public static class B extends A { }\n" +
            "    public static class C extends B {\n" +
            "        @Override public String who() { return \"C\"; }\n" +
            "    }\n" +
            "}\n";

    private static final String MAIN =
            "#include \"common.h\"\n" +
            "#include \"vp/V_C.h\"\n" +
            "#include <cstdio>\n" +
            "#include <string>\n" +
            "int main(){\n" +
            "    vp::V_C c;\n" +
            "    vp::V_A* a = &c;\n" +
            "    vp::V_B* b = &c;\n" +
            "    std::string s1 = *a->who(), s2 = *b->who(), s3 = *c.who();\n" +
            "    printf(\"%s %s %s\\n\", s1.c_str(), s2.c_str(), s3.c_str());\n" +
            "    return (s1==\"C\" && s2==\"C\" && s3==\"C\") ? 0 : 1;\n" +
            "}\n";

    @Test
    public void deepOverrideDispatchesVirtually() throws Exception {
        Path compiler = Arrays.stream(System.getenv("PATH").split(":"))
                .map(Path::of).map(d -> d.resolve("g++")).filter(Files::isExecutable)
                .findFirst().orElse(null);
        Assume.assumeTrue("g++ is not available", compiler != null);

        Path src = Files.createTempDirectory("j2cpp-vd-src");
        Files.createDirectories(src.resolve("vp"));
        Files.write(src.resolve("vp/V.java"), JAVA.getBytes(StandardCharsets.UTF_8));
        Path out = Files.createTempDirectory("j2cpp-vd-out");
        Cmd.main(new String[]{"-src", src.toString(), "-out", out.toString()});

        String base = new String(Files.readAllBytes(out.resolve("vp/V_A.h")), StandardCharsets.UTF_8);
        String sub = new String(Files.readAllBytes(out.resolve("vp/V_C.h")), StandardCharsets.UTF_8);
        assertTrue("base method not virtual:\n" + base, base.contains("virtual std::string* who()"));
        assertTrue("override not marked:\n" + sub, sub.contains("std::string* who() override"));

        Path main = out.resolve("main.cpp");
        Files.write(main, MAIN.getBytes(StandardCharsets.UTF_8));
        Path exe = out.resolve("vd_test");
        Process cp = new ProcessBuilder(compiler.toString(), "-std=c++17",
                "-I" + Path.of("cppRt").toAbsolutePath(), "-I" + out,
                out.resolve("src/vp/V.cpp").toString(), main.toString(), "-o", exe.toString())
                .redirectErrorStream(true).start();
        String compileLog = new String(cp.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals("compile failed:\n" + compileLog, 0,
                cp.waitFor(60, TimeUnit.SECONDS) ? cp.exitValue() : -1);

        Process run = new ProcessBuilder(exe.toString()).redirectErrorStream(true).start();
        String log = new String(run.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals("run output:\n" + log, 0, run.waitFor(60, TimeUnit.SECONDS) ? run.exitValue() : -1);
    }
}
