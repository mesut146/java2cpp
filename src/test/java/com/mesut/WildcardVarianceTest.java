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

/** Stage 3: List<Integer> is accepted where List<? extends Number> is expected. */
public class WildcardVarianceTest {

    private static final String JAVA = """
            package g;
            import java.util.*;
            public class G {
                public static <T> T identity(T x) { return x; }
                public static int count(List<? extends Number> xs) { return xs.size(); }
                public static int main(String[] a) {
                    int v = identity(5);
                    List<Integer> l = new ArrayList<Integer>();
                    l.add(1); l.add(2);
                    return v + count(l);
                }
            }
            """;

    private static final String MAIN = """
            #include "common.h"
            #include "g/G.h"
            #include <cstdio>
            int main(){
                int r = g::G::main(nullptr);
                printf("r=%d\\n", r);
                return r == 7 ? 0 : 1;
            }
            """;

    @Test
    public void wildcardParameterAcceptsConcreteList() throws Exception {
        Path compiler = Arrays.stream(System.getenv("PATH").split(":"))
                .map(Path::of).map(d -> d.resolve("g++")).filter(Files::isExecutable)
                .findFirst().orElse(null);
        Assume.assumeTrue("g++ is not available", compiler != null);

        Path src = Files.createTempDirectory("j2cpp-wv-src");
        Files.createDirectories(src.resolve("g"));
        Files.write(src.resolve("g/G.java"), JAVA.getBytes(StandardCharsets.UTF_8));
        Path out = Files.createTempDirectory("j2cpp-wv-out");
        Cmd.main(new String[]{"-src", src.toString(), "-out", out.toString()});

        String header = new String(Files.readAllBytes(out.resolve("g/G.h")), StandardCharsets.UTF_8);
        assertTrue("wildcard template missing:\n" + header, header.contains("template <class W0>"));
        assertTrue("wildcard element type not templated:\n" + header,
                header.contains("static int count(std::vector<W0>* xs)"));
        assertTrue("wildcard note missing:\n" + header,
                header.contains("wildcard type arguments templated: W0 extends java.lang.Number"));

        String log = new String(Files.readAllBytes(out.resolve("log.txt")), StandardCharsets.UTF_8);
        assertTrue("wildcard not logged:\n" + log, log.contains("W0 extends java.lang.Number"));

        String cpp = new String(Files.readAllBytes(out.resolve("src/g/G.cpp")), StandardCharsets.UTF_8);
        assertTrue("wildcard bound not enforced:\n" + cpp, cpp.contains(
                "static_assert(j2cpp::wildcard_ok<W0, CppHelper::Number*>::value"));

        Path main = out.resolve("main.cpp");
        Files.write(main, MAIN.getBytes(StandardCharsets.UTF_8));
        Path exe = out.resolve("wv_test");
        Process cp = new ProcessBuilder(compiler.toString(), "-std=c++17",
                "-I" + Path.of("cppRt").toAbsolutePath(), "-I" + out,
                out.resolve("src/g/G.cpp").toString(), main.toString(), "-o", exe.toString())
                .redirectErrorStream(true).start();
        String compileLog = new String(cp.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals("compile/link failed:\n" + compileLog, 0,
                cp.waitFor(60, TimeUnit.SECONDS) ? cp.exitValue() : -1);

        Process run = new ProcessBuilder(exe.toString()).redirectErrorStream(true).start();
        String runLog = new String(run.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals("run output:\n" + runLog, 0, run.waitFor(60, TimeUnit.SECONDS) ? run.exitValue() : -1);
    }

    @Test
    public void wildcardBoundRejectsWrongElementType() throws Exception {
        Path compiler = Arrays.stream(System.getenv("PATH").split(":"))
                .map(Path::of).map(d -> d.resolve("g++")).filter(Files::isExecutable)
                .findFirst().orElse(null);
        Assume.assumeTrue("g++ is not available", compiler != null);

        String invalid = """
                package g;
                import java.util.*;
                public class G {
                    public static int count(List<? extends Number> xs) { return xs.size(); }
                    public static int main(String[] a) {
                        List<String> l = new ArrayList<String>();
                        l.add("x");
                        return count(l);
                    }
                }
                """;
        Path src = Files.createTempDirectory("j2cpp-wv-neg-src");
        Files.createDirectories(src.resolve("g"));
        Files.write(src.resolve("g/G.java"), invalid.getBytes(StandardCharsets.UTF_8));
        Path out = Files.createTempDirectory("j2cpp-wv-neg-out");
        Cmd.main(new String[]{"-src", src.toString(), "-out", out.toString()});

        Process cp = new ProcessBuilder(compiler.toString(), "-std=c++17", "-fsyntax-only",
                "-I" + Path.of("cppRt").toAbsolutePath(), "-I" + out,
                out.resolve("src/g/G.cpp").toString())
                .redirectErrorStream(true).start();
        String log = new String(cp.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertTrue("expected the wildcard bound check to fail:\n" + log,
                cp.waitFor(60, TimeUnit.SECONDS) && cp.exitValue() != 0);
        assertTrue("static assertion message missing:\n" + log,
                log.contains("static assertion failed: W0 extends java.lang.Number"));
    }
}
