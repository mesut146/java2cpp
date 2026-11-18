package com.mesut;

import org.junit.Test;

import java.nio.file.Path;


/** Verifies that anonymous classes capture effectively-final locals at runtime. */
public class CaptureBehaviorTest {

    private static final String JAVA =
            "package c;\n" +
            "public class Cap {\n" +
            "    interface I { int run(); }\n" +
            "    int test() {\n" +
            "        final int base = 7;\n" +
            "        final String tag = \"T\";\n" +
            "        I i = new I() { public int run() { return base + tag.length(); } };\n" +
            "        return i.run();\n" +
            "    }\n" +
            "}\n";

    private static final String MAIN =
            "#include \"common.h\"\n" +
            "#include \"c/Cap.h\"\n" +
            "#include <cstdio>\n" +
            "int main(){ c::Cap o; int r=o.test(); printf(\"r=%d\\n\", r); return r==8?0:1; }\n";

    @Test
    public void anonymousClassCapturesLocals() throws Exception {
        Path compiler = CppTestSupport.compiler();

        Path out = CppTestSupport.convert(JAVA, "c", "Cap", "cap");

        CppTestSupport.compile(out, compiler, "src/c/Cap.cpp", MAIN, "cap_test")
                .assertOk("cap");
        CppTestSupport.runMerged(out.resolve("cap_test")).assertOk("cap");
    }
}
