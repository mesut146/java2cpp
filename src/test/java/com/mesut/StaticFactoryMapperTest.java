package com.mesut;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.Assert.assertTrue;

/** Verifies List.of/Set.of/Arrays.asList map to C++ container builders, including varargs. */
public class StaticFactoryMapperTest {

    private static final String JAVA =
            "package fp;\n" +
            "import java.util.Arrays;\n" +
            "import java.util.List;\n" +
            "import java.util.Set;\n" +
            "public class F {\n" +
            "    public static int main(String[] a) {\n" +
            "        List<String> l = List.of(\"a\", \"b\");\n" +
            "        Set<String> s = Set.of(\"x\", \"y\");\n" +
            "        List<String> al = Arrays.asList(\"p\", \"q\", \"r\");\n" +
            "        return l.size() * 100 + s.size() * 10 + al.size();\n" +
            "    }\n" +
            "}\n";

    private static final String MAIN =
            "#include \"common.h\"\n" +
            "#include \"fp/F.h\"\n" +
            "#include <cstdio>\n" +
            "int main(){\n" +
            "    int r = fp::F::main(nullptr);\n" +
            "    printf(\"r=%d\\n\", r);\n" +
            "    return r == 223 ? 0 : 1;\n" +
            "}\n";

    @Test
    public void collectionFactoriesMapToCppContainers() throws Exception {
        Path compiler = CppTestSupport.compiler();

        Path out = CppTestSupport.convert(JAVA, "fp", "F", "sf");

        String gen = new String(Files.readAllBytes(out.resolve("src/fp/F.cpp")), StandardCharsets.UTF_8);
        assertTrue("List.of not mapped:\n" + gen, gen.contains("CppHelper::from({"));
        assertTrue("Set.of not mapped:\n" + gen, gen.contains("CppHelper::fromSet({"));
        assertTrue("Arrays.asList not mapped:\n" + gen,
                count(gen, "CppHelper::from({") == 2);

        CppTestSupport.compile(out, compiler, "src/fp/F.cpp", MAIN, "sf_test")
                .assertOk("sf");
        CppTestSupport.runMerged(out.resolve("sf_test")).assertOk("sf");
    }

    private static int count(String text, String needle) {
        int n = 0, from = 0;
        while ((from = text.indexOf(needle, from)) >= 0) {
            n++;
            from += needle.length();
        }
        return n;
    }
}
