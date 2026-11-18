package com.mesut;

import org.junit.Test;

import java.nio.file.Path;


/** Verifies that an anonymous class body is emitted and its method overrides dispatch. */
public class AnonymousClassBehaviorTest {

    private static final String JAVA =
            "package b;\n" +
            "public class Behavior {\n" +
            "    interface I { int run(); }\n" +
            "    int call(I i) { return i.run(); }\n" +
            "    int test() { I i = new I() { public int run() { return 42; } }; return call(i); }\n" +
            "}\n";

    private static final String MAIN =
            "#include \"common.h\"\n" +
            "#include \"b/Behavior.h\"\n" +
            "#include <cstdio>\n" +
            "int main(){ b::Behavior o; int r=o.test(); printf(\"r=%d\\n\", r); return r==42?0:1; }\n";

    @Test
    public void anonymousClassOverrides() throws Exception {
        Path compiler = CppTestSupport.compiler();

        Path out = CppTestSupport.convert(JAVA, "b", "Behavior", "anon");

        CppTestSupport.compile(out, compiler, "src/b/Behavior.cpp", MAIN, "anon_test")
                .assertOk("anon");
        CppTestSupport.runMerged(out.resolve("anon_test")).assertOk("anon");
    }
}
