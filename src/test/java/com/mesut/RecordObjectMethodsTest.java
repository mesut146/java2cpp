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

/** Verifies the auto-generated toString/equals/hashCode on records. */
public class RecordObjectMethodsTest {

    private static final String JAVA =
            "package rp;\n" +
            "public class R {\n" +
            "    public record Point(int x, int y) {}\n" +
            "    public record Named(String name, Point p) {}\n" +
            "}\n";

    private static final String MAIN =
            "#include \"common.h\"\n" +
            "#include \"rp/R_Point.h\"\n" +
            "#include \"rp/R_Named.h\"\n" +
            "#include <cstdio>\n" +
            "#include <string>\n" +
            "int main(){\n" +
            "    rp::R_Point p(1,2), q(1,2), r(3,4);\n" +
            "    rp::R_Named n(new std::string(\"hi\"), &p);\n" +
            "    std::string ts = *p.toString();\n" +
            "    std::string nts = *n.toString();\n" +
            "    java::lang::Object* op = &p, *oq = &q, *oz = &r;\n" +
            "    std::string vts = *op->toString();\n" +
            "    printf(\"ts=%s eq=%d neq=%d h=%d n=%s vts=%s\\n\", ts.c_str(), p.equals(&q), p.equals(&r), p.hashCode(), nts.c_str(), vts.c_str());\n" +
            "    return (ts==\"R_Point[x=1, y=2]\" && p.equals(&q) && !p.equals(&r)\n" +
            "            && p.hashCode()==q.hashCode()\n" +
            "            && nts.find(\"name=hi\")!=std::string::npos\n" +
            "            && nts.find(\"p=R_Point[x=1, y=2]\")!=std::string::npos\n" +
            "            && vts==ts && op->equals(oq) && !op->equals(oz) && op->hashCode()==q.hashCode()) ? 0 : 1;\n" +
            "}\n";

    @Test
    public void recordObjectMethods() throws Exception {
        Path compiler = Arrays.stream(System.getenv("PATH").split(":"))
                .map(Path::of).map(d -> d.resolve("g++")).filter(Files::isExecutable)
                .findFirst().orElse(null);
        Assume.assumeTrue("g++ is not available", compiler != null);

        Path src = Files.createTempDirectory("j2cpp-rom-src");
        Files.createDirectories(src.resolve("rp"));
        Files.write(src.resolve("rp/R.java"), JAVA.getBytes(StandardCharsets.UTF_8));
        Path out = Files.createTempDirectory("j2cpp-rom-out");
        Cmd.main(new String[]{"-src", src.toString(), "-out", out.toString()});

        String objectHeader = new String(Files.readAllBytes(
                out.resolve("lib/java/lang/Object.h")), StandardCharsets.UTF_8);
        assertTrue("Object stub missing virtual toString:\n" + objectHeader,
                objectHeader.contains("virtual std::string* toString()"));
        assertTrue("Object stub missing virtual equals:\n" + objectHeader,
                objectHeader.contains("virtual bool equals(Object* o)"));
        assertTrue("Object stub missing virtual hashCode:\n" + objectHeader,
                objectHeader.contains("virtual int hashCode()"));

        Path main = out.resolve("main.cpp");
        Files.write(main, MAIN.getBytes(StandardCharsets.UTF_8));
        Path exe = out.resolve("rom_test");
        Process cp = new ProcessBuilder(compiler.toString(), "-std=c++17",
                "-I" + Path.of("cppRt").toAbsolutePath(), "-I" + out,
                out.resolve("src/rp/R.cpp").toString(), main.toString(), "-o", exe.toString())
                .redirectErrorStream(true).start();
        String compileLog = new String(cp.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals("compile failed:\n" + compileLog, 0,
                cp.waitFor(60, TimeUnit.SECONDS) ? cp.exitValue() : -1);

        Process run = new ProcessBuilder(exe.toString()).redirectErrorStream(true).start();
        String log = new String(run.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals("run output:\n" + log, 0, run.waitFor(60, TimeUnit.SECONDS) ? run.exitValue() : -1);
    }
}
