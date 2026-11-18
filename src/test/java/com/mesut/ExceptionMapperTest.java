package com.mesut;

import org.junit.Test;

import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/** Tier 3d: Throwable/Exception map to a real class with message, cause and stack trace. */
public class ExceptionMapperTest {

    private static final String JAVA = """
            package e;
            public class E {
                static class Boom extends RuntimeException {
                    Boom(String m) { super(m); }
                }
                static void fail() { throw new Boom("bang"); }
                public static int main(String[] a) {
                    int r = 0;
                    try {
                        fail();
                    } catch (Exception e) {
                        if ("bang".equals(e.getMessage())) r += 1;
                        Boom cause = new Boom("cause");
                        e.initCause(cause);
                        if ("cause".equals(e.getCause().getMessage())) r += 2;
                        if (e.toString().contains("bang")) r += 4;
                        e.addSuppressed(new Boom("supp"));
                        e.printStackTrace();
                    }
                    try {
                        throw new IllegalArgumentException("bad");
                    } catch (RuntimeException x) {
                        if ("bad".equals(x.getMessage())) r += 8;
                    }
                    return r;
                }
            }
            """;

    private static final String MAIN = """
            #include "common.h"
            #include "e/E.h"
            #include <cstdio>
            int main(){
                int r = e::E::main(nullptr);
                printf("r=%d\\n", r);
                return r == 15 ? 0 : 1;
            }
            """;

    @Test
    public void exceptionBehavior() throws Exception {
        Path compiler = CppTestSupport.compiler();
        Path out = CppTestSupport.convert(JAVA, "e", "E", "exm");

        String header = CppTestSupport.generated(out, "e/E_Boom.h");
        assertTrue("exception base not mapped:\n" + header,
                header.contains("public CppHelper::Throwable"));
        String cpp = CppTestSupport.generated(out, "src/e/E.cpp");
        assertTrue("throw still allocates:\n" + cpp, cpp.contains("throw E_Boom("));
        assertTrue("mapped throw still allocates:\n" + cpp, cpp.contains("throw CppHelper::Throwable("));

        CppTestSupport.compile(out, compiler, "src/e/E.cpp", MAIN, "exm_test")
                .assertOk("exception test");
        CppTestSupport.RunResult result = CppTestSupport.runSeparate(out.resolve("exm_test"));
        String stdout = result.stdout;
        String stderr = result.stderr;
        result.assertOk("exception test");
        assertEquals("stdout", "r=15\n", stdout);
        assertTrue("concrete class name missing:\n" + stderr, stderr.contains("E$Boom: bang"));
        assertTrue("stack frames missing:\n" + stderr, stderr.contains("\tat "));
        assertTrue("cause missing:\n" + stderr, stderr.contains("Caused by:"));
        assertTrue("suppressed missing:\n" + stderr, stderr.contains("Suppressed: e.E$Boom: supp"));
    }
}
