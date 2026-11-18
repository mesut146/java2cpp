package com.mesut;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertTrue;

/** Stage 2: method type parameters become C++ templates and T is deduced at call sites. */
public class GenericMethodTemplateTest {

    private static final String JAVA = """
            package g;
            public class G {
                public static <T> T identity(T x) { return x; }
                public static <T> T pick(T a, T b) { return a; }
                public static int main(String[] a) {
                    int v = identity(5);
                    String s = identity("ok");
                    int p = pick(3, 9);
                    return v + p + (s.equals("ok") ? 1 : 0);
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
                return r == 9 ? 0 : 1;
            }
            """;

    @Test
    public void genericMethodsCompileAndRun() throws Exception {
        Path compiler = CppTestSupport.compiler();

        Path out = CppTestSupport.convert(JAVA, "g", "G", "gtm");

        String header = new String(Files.readAllBytes(out.resolve("g/G.h")), StandardCharsets.UTF_8);
        assertTrue("templates missing:\n" + header, header.contains("template <class T>"));
        assertTrue("type variable not preserved:\n" + header, header.contains("static T identity(T x)"));

        CppTestSupport.compile(out, compiler, "src/g/G.cpp", MAIN, "gtm_test")
                .assertOk("gtm");
        CppTestSupport.runMerged(out.resolve("gtm_test")).assertOk("gtm");
    }
}
