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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/** Java interfaces map to abstract C++ classes with virtual dispatch. */
public class InterfaceBehaviorTest {

    private static final String JAVA = """
            package i;
            interface Greeter {
                int BONUS = 10;
                String greet();
                default String label() { return "[" + greet() + "]"; }
                static Greeter make(String s) { return new Impl(s); }
            }
            class Impl implements Greeter {
                private String s;
                Impl(String s) { this.s = s; }
                public String greet() { return s; }
            }
            class Loud implements Greeter {
                public String greet() { return "x"; }
                public String label() { return "OVERRIDE"; }
            }
            public class I {
                public static int main(String[] a) {
                    Greeter g = Greeter.make("hi");
                    Greeter other = new Greeter() { public String greet() { return "anon"; } };
                    Greeter loud = new Loud();
                    Object o = g;
                    Greeter cast = (Greeter) o;
                    return cast.label().length() + other.label().length()
                            + loud.label().length() + Greeter.BONUS;
                }
            }
            """;

    private static final String MAIN = """
            #include "common.h"
            #include "i/I.h"
            #include <cstdio>
            int main(){
                int r = i::I::main(nullptr);
                printf("r=%d\\n", r);
                return r == 28 ? 0 : 1;
            }
            """;

    @Test
    public void defaultStaticMethodsAndConstantsWork() throws Exception {
        Path compiler = Arrays.stream(System.getenv("PATH").split(":"))
                .map(Path::of).map(d -> d.resolve("g++")).filter(Files::isExecutable)
                .findFirst().orElse(null);
        Assume.assumeTrue("g++ is not available", compiler != null);

        Path src = Files.createTempDirectory("j2cpp-iface-src");
        Files.createDirectories(src.resolve("i"));
        Files.write(src.resolve("i/I.java"), JAVA.getBytes(StandardCharsets.UTF_8));
        Path out = Files.createTempDirectory("j2cpp-iface-out");
        Cmd.main(new String[]{"-src", src.toString(), "-out", out.toString()});

        String header = new String(Files.readAllBytes(out.resolve("i/Greeter.h")), StandardCharsets.UTF_8);
        assertTrue("abstract method should stay pure virtual:\n" + header,
                header.contains("virtual std::string* greet() = 0"));
        assertTrue("default method should be concrete and virtual:\n" + header,
                header.contains("virtual std::string* label()"));
        assertFalse("default method should not be pure virtual:\n" + header,
                header.contains("label() = 0"));
        assertTrue("static method should not be virtual:\n" + header,
                header.contains("static Greeter* make("));
        assertFalse("illegal static virtual emitted:\n" + header,
                header.contains("static virtual"));
        assertTrue("interface constant should be constexpr:\n" + header,
                header.contains("static constexpr int BONUS = 10"));

        Path main = out.resolve("main.cpp");
        Files.write(main, MAIN.getBytes(StandardCharsets.UTF_8));
        Path exe = out.resolve("iface_test");
        Process cp = new ProcessBuilder(compiler.toString(), "-std=c++17",
                "-I" + Path.of("cppRt").toAbsolutePath(), "-I" + out,
                out.resolve("src/i/I.cpp").toString(), main.toString(), "-o", exe.toString())
                .redirectErrorStream(true).start();
        String compileLog = new String(cp.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals("compile/link failed:\n" + compileLog, 0,
                cp.waitFor(60, TimeUnit.SECONDS) ? cp.exitValue() : -1);

        Process run = new ProcessBuilder(exe.toString()).redirectErrorStream(true).start();
        String log = new String(run.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals("run output:\n" + log, 0, run.waitFor(60, TimeUnit.SECONDS) ? run.exitValue() : -1);
    }
}
