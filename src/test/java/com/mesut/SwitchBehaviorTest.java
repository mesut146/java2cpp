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

/** Verifies that generated switch statements behave like Java switches. */
public class SwitchBehaviorTest {

    private static final String JAVA =
            "package s;\n" +
            "public class S {\n" +
            "    static int last = 0;\n" +
            "    int f(int x) {\n" +
            "        switch (x) {\n" +
            "            case 1: last = 1; return 10;\n" +
            "            case 2: last = 2; return 20;\n" +
            "            default: last = 3; return 30;\n" +
            "        }\n" +
            "    }\n" +
            "}\n";

    private static final String MAIN =
            "#include \"common.h\"\n" +
            "#include \"s/S.h\"\n" +
            "#include <cstdio>\n" +
            "int main() {\n" +
            "    s::S obj; int fails = 0;\n" +
            "    s::S::last=0; int a=obj.f(1); if(a!=10||s::S::last!=1){printf(\"f1=%d last=%d\\n\",a,s::S::last);fails++;}\n" +
            "    s::S::last=0; int b=obj.f(2); if(b!=20||s::S::last!=2){printf(\"f2=%d last=%d\\n\",b,s::S::last);fails++;}\n" +
            "    s::S::last=0; int c=obj.f(9); if(c!=30||s::S::last!=3){printf(\"f9=%d last=%d\\n\",c,s::S::last);fails++;}\n" +
            "    printf(\"fails=%d\\n\", fails);\n" +
            "    return fails;\n" +
            "}\n";

    @Test
    public void switchBehavior() throws Exception {
        Path compiler = Arrays.stream(System.getenv("PATH").split(":"))
                .map(Path::of).map(d -> d.resolve("g++")).filter(Files::isExecutable)
                .findFirst().orElse(null);
        Assume.assumeTrue("g++ is not available", compiler != null);

        Path src = Files.createTempDirectory("j2cpp-sw-src");
        Files.createDirectories(src.resolve("s"));
        Files.write(src.resolve("s/S.java"), JAVA.getBytes(StandardCharsets.UTF_8));
        Path out = Files.createTempDirectory("j2cpp-sw-out");
        Cmd.main(new String[]{"-src", src.toString(), "-out", out.toString()});

        Path main = out.resolve("main.cpp");
        Files.write(main, MAIN.getBytes(StandardCharsets.UTF_8));
        Path exe = out.resolve("sw_test");
        Process cp = new ProcessBuilder(compiler.toString(), "-std=c++17",
                "-I" + Path.of("cppRt").toAbsolutePath(), "-I" + out,
                out.resolve("src/s/S.cpp").toString(), main.toString(), "-o", exe.toString())
                .redirectErrorStream(true).start();
        String compileLog = new String(cp.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals("compile failed:\n" + compileLog, 0,
                cp.waitFor(60, TimeUnit.SECONDS) ? cp.exitValue() : -1);

        Process run = new ProcessBuilder(exe.toString()).redirectErrorStream(true).start();
        String log = new String(run.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals("run output:\n" + log, 0, run.waitFor(60, TimeUnit.SECONDS) ? run.exitValue() : -1);
    }
}
