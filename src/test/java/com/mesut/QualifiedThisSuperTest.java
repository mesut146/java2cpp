package com.mesut;

import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.Assert.assertTrue;

/** Outer.this / Outer.super resolve through the enclosing-instance reference. */
public class QualifiedThisSuperTest {

    private static final String JAVA = """
            package q;
            class Base {
                int f = 1;
                String m() { return "base"; }
            }
            class Outer extends Base {
                String m() { return "outer"; }
                class Inner {
                    String test() { return Outer.this.m() + ":" + Outer.super.m(); }
                    int field() { return Outer.super.f + Outer.this.f; }
                }
            }
            public class Q {
                public static int main(String[] a) {
                    Outer o = new Outer();
                    String s = o.new Inner().test();
                    return s.equals("outer:base") ? 1 : 0;
                }
            }
            """;

    private static final String MAIN = """
            #include "common.h"
            #include "q/Q.h"
            #include <cstdio>
            int main(){
                int r = q::Q::main(nullptr);
                printf("r=%d\\n", r);
                return r == 1 ? 0 : 1;
            }
            """;

    @Test
    public void qualifiedThisAndSuperUseParentReference() throws Exception {
        Path compiler = CppTestSupport.compiler();
        Path out = CppTestSupport.convert(JAVA, "q", "Q", "qts");

        String log = CppTestSupport.log(out);
        assertTrue("qualified this/super still logged as unsupported:\n" + log,
                !log.contains("qualified this is not supported")
                        && !log.contains("qualified super method invocation is not supported")
                        && !log.contains("qualified super field access is not supported"));

        String cpp = CppTestSupport.generated(out, "src/q/Q.cpp");
        assertTrue("outer this not resolved:\n" + cpp, cpp.contains("_parent_ref->m()"));
        assertTrue("qualified super method not resolved:\n" + cpp,
                cpp.contains("_parent_ref->q::Base::m()"));
        assertTrue("qualified super field not resolved:\n" + cpp,
                cpp.contains("_parent_ref->q::Base::f"));

        List<Path> sources;
        try (Stream<Path> stream = Files.list(out.resolve("src/q"))) {
            sources = stream.filter(p -> p.toString().endsWith(".cpp")).toList();
        }
        CppTestSupport.compile(out, compiler, sources, MAIN, "qts_test")
                .assertOk("qualified this/super test");
        CppTestSupport.runMerged(out.resolve("qts_test")).assertOk("qualified this/super test");
    }
}
