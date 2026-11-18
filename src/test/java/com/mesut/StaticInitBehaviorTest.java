package com.mesut;

import org.junit.Test;

import java.nio.file.Path;


/** Verifies that a static initializer is emitted as si() and updates static state. */
public class StaticInitBehaviorTest {

    private static final String JAVA =
            "package s;\n" +
            "public class St {\n" +
            "    static int counter = 0;\n" +
            "    static { counter = 5; }\n" +
            "}\n";

    private static final String MAIN =
            "#include \"common.h\"\n" +
            "#include \"s/St.h\"\n" +
            "#include <cstdio>\n" +
            "int main(){ s::St::si(); printf(\"counter=%d\\n\", s::St::counter); return s::St::counter==5?0:1; }\n";

    @Test
    public void staticInitializerRuns() throws Exception {
        Path compiler = CppTestSupport.compiler();

        Path out = CppTestSupport.convert(JAVA, "s", "St", "si");

        CppTestSupport.compile(out, compiler, "src/s/St.cpp", MAIN, "si_test")
                .assertOk("si");
        CppTestSupport.runMerged(out.resolve("si_test")).assertOk("si");
    }
}
