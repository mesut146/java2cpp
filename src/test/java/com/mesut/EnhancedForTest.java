package com.mesut;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertTrue;

/** Verifies enhanced-for over an expression evaluates it once and iterates a mapped collection. */
public class EnhancedForTest {

    private static final String JAVA =
            "package ef;\n" +
            "import java.util.ArrayList;\n" +
            "import java.util.List;\n" +
            "public class E {\n" +
            "    static int calls = 0;\n" +
            "    static List<String> getList() {\n" +
            "        calls++;\n" +
            "        List<String> l = new ArrayList<String>();\n" +
            "        l.add(\"a\");\n" +
            "        l.add(\"b\");\n" +
            "        return l;\n" +
            "    }\n" +
            "    public static int main(String[] args) {\n" +
            "        int count = 0;\n" +
            "        for (String s : getList()) { count = count + 1; }\n" +
            "        return calls * 10 + count;\n" +
            "    }\n" +
            "}\n";

    private static final String MAIN =
            "#include \"common.h\"\n" +
            "#include \"ef/E.h\"\n" +
            "#include <cstdio>\n" +
            "int main(){\n" +
            "    int r = ef::E::main(nullptr);\n" +
            "    printf(\"r=%d\\n\", r);\n" +
            "    return r == 12 ? 0 : 1;\n" +
            "}\n";

    @Test
    public void enhancedForEvaluatesExpressionOnce() throws Exception {
        Path compiler = CppTestSupport.compiler();

        Path out = CppTestSupport.convert(JAVA, "ef", "E", "ef");

        String gen = new String(Files.readAllBytes(out.resolve("src/ef/E.cpp")), StandardCharsets.UTF_8);
        assertTrue("expression re-evaluated in loop:\n" + gen,
                countOccurrences(gen, "E::getList();") == 1);

        CppTestSupport.compile(out, compiler, "src/ef/E.cpp", MAIN, "ef_test")
                .assertOk("ef");
        CppTestSupport.runMerged(out.resolve("ef_test")).assertOk("ef");
    }

    private static int countOccurrences(String text, String needle) {
        int count = 0, from = 0;
        while ((from = text.indexOf(needle, from)) >= 0) {
            count++;
            from += needle.length();
        }
        return count;
    }
}
