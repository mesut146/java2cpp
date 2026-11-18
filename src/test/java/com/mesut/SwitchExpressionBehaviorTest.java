package com.mesut;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;

/** Verifies that a switch expression is translated to a value-producing lambda. */
public class SwitchExpressionBehaviorTest {

    private static final String JAVA =
            "package s;\n" +
            "public class SE {\n" +
            "    int classify(int x) {\n" +
            "        int result = switch (x) {\n" +
            "            case 1 -> 10;\n" +
            "            case 2 -> 20;\n" +
            "            default -> 30;\n" +
            "        };\n" +
            "        return result;\n" +
            "    }\n" +
            "}\n";

    private static final String MAIN =
            "#include \"common.h\"\n" +
            "#include \"s/SE.h\"\n" +
            "#include <cstdio>\n" +
            "int main(){\n" +
            "    s::SE o;\n" +
            "    int a=o.classify(1), b=o.classify(2), c=o.classify(9);\n" +
            "    printf(\"a=%d b=%d c=%d\\n\", a, b, c);\n" +
            "    return (a==10 && b==20 && c==30) ? 0 : 1;\n" +
            "}\n";

    @Test
    public void switchExpressionProducesValue() throws Exception {
        Path compiler = CppTestSupport.compiler();

        Path out = CppTestSupport.convert(JAVA, "s", "SE", "se");

        String cpp = new String(Files.readAllBytes(out.resolve("src/s/SE.cpp")), StandardCharsets.UTF_8);
        assertEquals("lambda not emitted: " + cpp, 1, cpp.indexOf("[&]() -> int") >= 0 ? 1 : 0);

        CppTestSupport.compile(out, compiler, "src/s/SE.cpp", MAIN, "se_test")
                .assertOk("se");
        CppTestSupport.runMerged(out.resolve("se_test")).assertOk("se");
    }
}
