package com.mesut;

import org.junit.Test;

import java.nio.file.Path;

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
        Path compiler = CppTestSupport.compiler();
        Path out = CppTestSupport.convert(JAVA, "g", "G", "wv");

        String header = CppTestSupport.generated(out, "g/G.h");
        assertTrue("wildcard template missing:\n" + header, header.contains("template <class W0>"));
        assertTrue("wildcard element type not templated:\n" + header,
                header.contains("static int count(std::vector<W0>* xs)"));
        assertTrue("wildcard note missing:\n" + header,
                header.contains("wildcard type arguments templated: W0 extends java.lang.Number"));

        String log = CppTestSupport.log(out);
        assertTrue("wildcard not logged:\n" + log, log.contains("W0 extends java.lang.Number"));

        String cpp = CppTestSupport.generated(out, "src/g/G.cpp");
        assertTrue("wildcard bound not enforced:\n" + cpp, cpp.contains(
                "static_assert(j2cpp::wildcard_ok<W0, CppHelper::Number*>::value"));

        CppTestSupport.compile(out, compiler, "src/g/G.cpp", MAIN, "wv_test")
                .assertOk("wildcard test");
        CppTestSupport.runMerged(out.resolve("wv_test")).assertOk("wildcard test");
    }

    @Test
    public void wildcardBoundRejectsWrongElementType() throws Exception {
        Path compiler = CppTestSupport.compiler();

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
        Path out = CppTestSupport.convert(invalid, "g", "G", "wv-neg");

        CppTestSupport.RunResult result = CppTestSupport.compileOnly(out, compiler,
                java.util.List.of(out.resolve("src/g/G.cpp")));
        String log = result.output;
        assertTrue("expected the wildcard bound check to fail:\n" + log, result.exit != 0);
        assertTrue("static assertion message missing:\n" + log,
                log.contains("static assertion failed: W0 extends java.lang.Number"));
    }
}
