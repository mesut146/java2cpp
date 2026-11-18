package com.mesut;

import org.junit.Test;

import java.nio.file.Path;


/** Verifies that a record generates fields, constructor, accessors and method bodies. */
public class RecordBehaviorTest {

    private static final String JAVA =
            "package r;\n" +
            "public class Rect {\n" +
            "    public record Circle(double radius) {\n" +
            "        public double scaled(double k) { return radius * k; }\n" +
            "    }\n" +
            "}\n";

    private static final String MAIN =
            "#include \"common.h\"\n" +
            "#include \"r/Rect_Circle.h\"\n" +
            "#include <cstdio>\n" +
            "int main(){\n" +
            "    r::Rect_Circle c(2.0);\n" +
            "    printf(\"r=%f scaled=%f\\n\", c.radius(), c.scaled(3.0));\n" +
            "    return (c.radius() == 2.0 && c.scaled(3.0) == 6.0) ? 0 : 1;\n" +
            "}\n";

    @Test
    public void recordAccessorsAndMethods() throws Exception {
        Path compiler = CppTestSupport.compiler();

        Path out = CppTestSupport.convert(JAVA, "r", "Rect", "rec");

        CppTestSupport.compile(out, compiler, "src/r/Rect.cpp", MAIN, "rec_test")
                .assertOk("rec");
        CppTestSupport.runMerged(out.resolve("rec_test")).assertOk("rec");
    }
}
