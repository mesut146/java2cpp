package com.mesut;

import org.junit.Test;

import javax.tools.ToolProvider;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;

/** Generated stub constructors must forward to a parameterized base constructor. */
public class StubConstructorTest {

    private static final String BASE = """
            package lib;
            public class Base {
                public Base(int v) { }
            }
            """;

    private static final String CHILD = """
            package lib;
            class Child extends Base {
                public Child(int v) { super(v); }
            }
            """;

    private static final String JAVA = """
            package use;
            public class U {
                public static int main(String[] a) {
                    return new lib.Child(5) == null ? 0 : 1;
                }
            }
            """;

    private static final String MAIN = """
            #include "common.h"
            #include "use/U.h"
            #include <cstdio>
            int main(){
                int r = use::U::main(nullptr);
                printf("r=%d\\n", r);
                return r == 1 ? 0 : 1;
            }
            """;

    @Test
    public void stubConstructorForwardsToBase() throws Exception {
        Path libSrc = Files.createTempDirectory("j2cpp-stubctor-libsrc");
        Files.createDirectories(libSrc.resolve("lib"));
        Path base = libSrc.resolve("lib/Base.java");
        Path child = libSrc.resolve("lib/Child.java");
        Files.write(base, BASE.getBytes(StandardCharsets.UTF_8));
        Files.write(child, CHILD.getBytes(StandardCharsets.UTF_8));
        Path classes = Files.createTempDirectory("j2cpp-stubctor-classes");
        assertEquals("javac failed", 0, ToolProvider.getSystemJavaCompiler()
                .run(null, null, null, "-d", classes.toString(), base.toString(), child.toString()));
        Path jar = Files.createTempDirectory("j2cpp-stubctor-jar").resolve("lib.jar");
        assertEquals("jar failed", 0, new ProcessBuilder("jar", "cf", jar.toString(),
                "-C", classes.toString(), ".").redirectErrorStream(true).start().waitFor());

        Path compiler = CppTestSupport.compiler();
        Path out = CppTestSupport.convertWithClasspath(JAVA, "use", "U", "stubctor", jar);

        String stub = CppTestSupport.generated(out, "lib/lib/Child.h");
        CppTestSupport.assertContains(stub, "Child(int p0) : Base() {}");

        CppTestSupport.compile(out, compiler, "src/use/U.cpp", MAIN, "stubctor_test")
                .assertOk("stub constructor test");
        CppTestSupport.runMerged(out.resolve("stubctor_test")).assertOk("stub constructor test");
    }
}
