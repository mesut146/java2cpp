package com.mesut;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertTrue;

/** Resources must be closed before an explicit finally, including on exception. */
public class FinallyOrderingTest {

    private static final String JAVA_TEMPLATE = """
            package f;
            import java.io.*;
            import java.nio.file.*;
            public class F {
                public static int main(String[] a) throws Exception {
                    int r = 0;
                    Path p = Paths.get("__BASE__/out.txt");
                    try {
                        try (PrintWriter pw = new PrintWriter("__BASE__/out.txt")) {
                            pw.println("body");
                            throw new RuntimeException("boom");
                        } finally {
                            String s = Files.readString(p);
                            if (s.equals("body\\n")) r += 1;
                        }
                    } catch (RuntimeException e) {
                        r += 2;
                    }
                    return r;
                }
            }
            """;

    private static final String MAIN = """
            #include "common.h"
            #include "f/F.h"
            #include <cstdio>
            int main(){
                int r = f::F::main(nullptr);
                printf("r=%d\\n", r);
                return r == 3 ? 0 : 1;
            }
            """;

    @Test
    public void resourcesCloseBeforeFinally() throws Exception {
        Path compiler = CppTestSupport.compiler();

        Path base = Files.createTempDirectory("j2cpp-finorder-base");
        String java = JAVA_TEMPLATE.replace("__BASE__", base.toString());

        Path out = CppTestSupport.convert(java, "f", "F", "finorder");

        String gen = new String(Files.readAllBytes(out.resolve("src/f/F.cpp")), StandardCharsets.UTF_8);
        assertTrue("guard missing:\n" + gen, gen.contains("CppHelper::ScopeGuard"));
        int lambda = gen.indexOf("[&]() -> std::optional<int>");
        int guard = gen.indexOf("CppHelper::ScopeGuard");
        assertTrue("guard not inside try lambda:\n" + gen, lambda >= 0 && guard > lambda);

        CppTestSupport.compile(out, compiler, "src/f/F.cpp", MAIN, "finorder_test")
                .assertOk("finorder");
        CppTestSupport.runMerged(out.resolve("finorder_test")).assertOk("finorder");
    }
}
