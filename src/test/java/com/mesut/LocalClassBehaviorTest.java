package com.mesut;

import org.junit.Test;

import java.nio.file.Path;


/** Verifies that a local class declared in a method captures locals at runtime. */
public class LocalClassBehaviorTest {

    private static final String JAVA =
            "package l;\n" +
            "public class Local {\n" +
            "    int test(int x) {\n" +
            "        final int base = x;\n" +
            "        class Helper {\n" +
            "            int compute(int v) { return v + base; }\n" +
            "        }\n" +
            "        Helper h = new Helper();\n" +
            "        return h.compute(5);\n" +
            "    }\n" +
            "}\n";

    private static final String MAIN =
            "#include \"common.h\"\n" +
            "#include \"l/Local.h\"\n" +
            "#include <cstdio>\n" +
            "int main(){ l::Local o; int r=o.test(7); printf(\"r=%d\\n\", r); return r==12?0:1; }\n";

    @Test
    public void localClassCapturesLocals() throws Exception {
        Path compiler = CppTestSupport.compiler();

        Path out = CppTestSupport.convert(JAVA, "l", "Local", "lc");

        CppTestSupport.compile(out, compiler, "src/l/Local.cpp", MAIN, "lc_test")
                .assertOk("lc");
        CppTestSupport.runMerged(out.resolve("lc_test")).assertOk("lc");
    }
}
