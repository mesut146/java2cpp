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

/** Verifies that the String mapper methods behave like their Java counterparts. */
public class StringMapperBehaviorTest {

    private static final String JAVA =
            "package s;\n" +
            "public class Str {\n" +
            "    public String upper(String x) { return x.toUpperCase(); }\n" +
            "    public boolean contains(String x, String y) { return x.contains(y); }\n" +
            "    public int index(String x, String y) { return x.indexOf(y); }\n" +
            "    public String replace(String x, String a, String b) { return x.replace(a, b); }\n" +
            "}\n";

    private static final String MAIN =
            "#include \"common.h\"\n" +
            "#include \"s/Str.h\"\n" +
            "#include <cstdio>\n" +
            "#include <string>\n" +
            "int main(){\n" +
            "    s::Str o;\n" +
            "    std::string x = \"Hello World\";\n" +
            "    std::string y = \"World\";\n" +
            "    std::string* r = o.upper(&x);\n" +
            "    bool c = o.contains(&x, &y);\n" +
            "    int idx = o.index(&x, &y);\n" +
            "    std::string a=\"l\", b=\"L\";\n" +
            "    std::string* rep = o.replace(&x, &a, &b);\n" +
            "    printf(\"upper=%s contains=%d index=%d replaced=%s\\n\", r->c_str(), c, idx, rep->c_str());\n" +
            "    return (std::string(*r)==\"HELLO WORLD\" && c && idx==6 && std::string(*rep)==\"HeLLo WorLd\") ? 0 : 1;\n" +
            "}\n";

    @Test
    public void stringMappersBehave() throws Exception {
        Path compiler = Arrays.stream(System.getenv("PATH").split(":"))
                .map(Path::of).map(d -> d.resolve("g++")).filter(Files::isExecutable)
                .findFirst().orElse(null);
        Assume.assumeTrue("g++ is not available", compiler != null);

        Path src = Files.createTempDirectory("j2cpp-str-src");
        Files.createDirectories(src.resolve("s"));
        Files.write(src.resolve("s/Str.java"), JAVA.getBytes(StandardCharsets.UTF_8));
        Path out = Files.createTempDirectory("j2cpp-str-out");
        Cmd.main(new String[]{"-src", src.toString(), "-out", out.toString()});

        Path main = out.resolve("main.cpp");
        Files.write(main, MAIN.getBytes(StandardCharsets.UTF_8));
        Path exe = out.resolve("str_test");
        Process cp = new ProcessBuilder(compiler.toString(), "-std=c++17",
                "-I" + Path.of("cppRt").toAbsolutePath(), "-I" + out,
                out.resolve("src/s/Str.cpp").toString(), main.toString(), "-o", exe.toString())
                .redirectErrorStream(true).start();
        String compileLog = new String(cp.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals("compile failed:\n" + compileLog, 0,
                cp.waitFor(60, TimeUnit.SECONDS) ? cp.exitValue() : -1);

        Process run = new ProcessBuilder(exe.toString()).redirectErrorStream(true).start();
        String log = new String(run.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals("run output:\n" + log, 0, run.waitFor(60, TimeUnit.SECONDS) ? run.exitValue() : -1);
    }
}
