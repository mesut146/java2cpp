package com.mesut;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertTrue;

/** Verifies static field writes, String += and defined cofui accessors for static finals. */
public class StaticFieldTest {

    private static final String JAVA =
            "package sa;\n" +
            "public class S {\n" +
            "    static String seen = \"\";\n" +
            "    static final String TAG = \"tag\";\n" +
            "    static int count = 0;\n" +
            "    public static int main(String[] a) {\n" +
            "        seen += \"x\";\n" +
            "        count += 2;\n" +
            "        String t = TAG;\n" +
            "        return count + seen.length() + t.length();\n" +
            "    }\n" +
            "}\n";

    private static final String MAIN =
            "#include \"common.h\"\n" +
            "#include \"sa/S.h\"\n" +
            "#include <cstdio>\n" +
            "int main(){\n" +
            "    int r = sa::S::main(nullptr);\n" +
            "    printf(\"r=%d\\n\", r);\n" +
            "    return r == 6 ? 0 : 1;\n" +
            "}\n";

    @Test
    public void staticFieldsAndStringPlusEquals() throws Exception {
        Path compiler = CppTestSupport.compiler();

        Path out = CppTestSupport.convert(JAVA, "sa", "S", "sf2");

        String gen = new String(Files.readAllBytes(out.resolve("src/sa/S.cpp")), StandardCharsets.UTF_8);
        assertTrue("static field emitted as accessor:\n" + gen, gen.contains("S::seen = "));
        assertTrue("String += not concatenated:\n" + gen, gen.contains("CppHelper::concat(S::seen"));
        assertTrue("cofui accessor not defined:\n" + gen, gen.contains("std::string* S::TAG()"));

        CppTestSupport.compile(out, compiler, "src/sa/S.cpp", MAIN, "sf2_test")
                .assertOk("sf2");
        CppTestSupport.runMerged(out.resolve("sf2_test")).assertOk("sf2");
    }
}
