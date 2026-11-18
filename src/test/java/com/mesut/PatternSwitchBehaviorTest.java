package com.mesut;

import org.junit.Test;

import java.nio.file.Path;


/** Verifies that a switch over sealed records with type patterns dispatches correctly. */
public class PatternSwitchBehaviorTest {

    private static final String JAVA =
            "package m;\n" +
            "public class Modern {\n" +
            "    sealed interface Shape permits Circle, Square {}\n" +
            "    record Circle(double r) implements Shape {}\n" +
            "    record Square(double s) implements Shape {}\n" +
            "    static int kind(Shape shape) {\n" +
            "        return switch (shape) {\n" +
            "            case Circle c -> 1;\n" +
            "            case Square q -> 2;\n" +
            "        };\n" +
            "    }\n" +
            "}\n";

    private static final String MAIN =
            "#include \"common.h\"\n" +
            "#include \"m/Modern.h\"\n" +
            "#include \"m/Modern_Circle.h\"\n" +
            "#include \"m/Modern_Square.h\"\n" +
            "#include <cstdio>\n" +
            "int main(){\n" +
            "    m::Modern_Circle c(2.0); m::Modern_Square s(3.0);\n" +
            "    int a=m::Modern::kind(&c), b=m::Modern::kind(&s);\n" +
            "    printf(\"a=%d b=%d\\n\", a, b);\n" +
            "    return (a==1 && b==2) ? 0 : 1;\n" +
            "}\n";

    @Test
    public void patternSwitchDispatches() throws Exception {
        Path compiler = CppTestSupport.compiler();

        Path out = CppTestSupport.convert(JAVA, "m", "Modern", "ps");

        CppTestSupport.compile(out, compiler, "src/m/Modern.cpp", MAIN, "ps_test")
                .assertOk("ps");
        CppTestSupport.runMerged(out.resolve("ps_test")).assertOk("ps");
    }
}
