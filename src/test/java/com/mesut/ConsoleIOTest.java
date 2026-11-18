package com.mesut;

import org.junit.Test;

import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/** Tier 3a: System.out/err must compile, link and print to real C++ streams. */
public class ConsoleIOTest {

    private static final String JAVA = """
            package p;
            public class P {
                static class A { public String toString() { return "A!"; } }
                public static void main(String[] a) {
                    System.out.print("x");
                    System.out.print(7);
                    System.out.println();
                    System.out.println(true);
                    System.out.println(3.5);
                    System.out.println(new A());
                    System.out.printf("%d-%s%n", 5, "ok");
                    System.err.println("err");
                }
            }
            """;

    private static final String MAIN = """
            #include "common.h"
            #include "p/P.h"
            int main(){ p::P::main(nullptr); return 0; }
            """;

    @Test
    public void consoleOutputLinksAndMatchesJava() throws Exception {
        Path compiler = CppTestSupport.compiler();
        Path out = CppTestSupport.convert(JAVA, "p", "P", "io");

        String gen = CppTestSupport.generated(out, "src/p/P.cpp");
        assertTrue("System.out not mapped:\n" + gen, gen.contains("CppHelper::javaOut()"));
        assertTrue("System.err not mapped:\n" + gen, gen.contains("CppHelper::javaErr()"));

        CppTestSupport.compile(out, compiler, "src/p/P.cpp", MAIN, "io_test")
                .assertOk("console I/O test");
        CppTestSupport.RunResult result = CppTestSupport.runSeparate(out.resolve("io_test"));
        String stdout = result.stdout;
        String stderr = result.stderr;
        result.assertOk("console I/O test");
        assertEquals("stdout", "x7\ntrue\n3.5\nA!\n5-ok\n", stdout);
        assertEquals("stderr", "err\n", stderr);
    }
}
