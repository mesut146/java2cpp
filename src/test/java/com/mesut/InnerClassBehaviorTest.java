package com.mesut;

import org.junit.Test;

import java.nio.file.Path;


/** Verifies that an inner class can read and write an enclosing field at runtime. */
public class InnerClassBehaviorTest {

    private static final String JAVA =
            "package i;\n" +
            "public class Inner {\n" +
            "    int value = 10;\n" +
            "    class Child {\n" +
            "        int read() { return value; }\n" +
            "        void write(int v) { value = v; }\n" +
            "    }\n" +
            "    int test() {\n" +
            "        Child c = new Child();\n" +
            "        int a = c.read();\n" +
            "        c.write(99);\n" +
            "        return a * 100 + value;\n" +
            "    }\n" +
            "}\n";

    private static final String MAIN =
            "#include \"common.h\"\n" +
            "#include \"i/Inner.h\"\n" +
            "#include <cstdio>\n" +
            "int main(){\n" +
            "    i::Inner o; o.value = 10;\n" +
            "    int r = o.test();\n" +
            "    printf(\"r=%d value=%d\\n\", r, o.value);\n" +
            "    return (r == 1099 && o.value == 99) ? 0 : 1;\n" +
            "}\n";

    @Test
    public void innerClassAccessesOuterField() throws Exception {
        Path compiler = CppTestSupport.compiler();

        Path out = CppTestSupport.convert(JAVA, "i", "Inner", "inner");

        CppTestSupport.compile(out, compiler, "src/i/Inner.cpp", MAIN, "inner_test")
                .assertOk("inner");
        CppTestSupport.runMerged(out.resolve("inner_test")).assertOk("inner");
    }
}
