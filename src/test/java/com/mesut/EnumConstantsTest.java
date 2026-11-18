package com.mesut;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertTrue;

/** Verifies enum constants are defined/initialized and expose ordinal/name/toString/compareTo. */
public class EnumConstantsTest {

    private static final String JAVA =
            "package ep;\n" +
            "public class E {\n" +
            "    public enum Color {\n" +
            "        RED(10), GREEN(20), BLUE(30);\n" +
            "        int code;\n" +
            "        Color(int code) { this.code = code; }\n" +
            "    }\n" +
            "    public static int main(String[] a) {\n" +
            "        Color c = Color.GREEN;\n" +
            "        return c.ordinal() * 100 + c.code\n" +
            "             + (c.name().equals(\"GREEN\") ? 1000 : 0)\n" +
            "             + (c.toString().equals(\"GREEN\") ? 10000 : 0)\n" +
            "             + (c.compareTo(Color.BLUE) < 0 ? 100000 : 0);\n" +
            "    }\n" +
            "}\n";

    private static final String MAIN =
            "#include \"common.h\"\n" +
            "#include \"ep/E.h\"\n" +
            "#include <cstdio>\n" +
            "int main(){\n" +
            "    int r = ep::E::main(nullptr);\n" +
            "    printf(\"r=%d\\n\", r);\n" +
            "    return r == 111120 ? 0 : 1;\n" +
            "}\n";

    @Test
    public void enumConstantsAreInitialized() throws Exception {
        Path compiler = CppTestSupport.compiler();

        Path out = CppTestSupport.convert(JAVA, "ep", "E", "en");

        String gen = new String(Files.readAllBytes(out.resolve("src/ep/E.cpp")), StandardCharsets.UTF_8);
        assertTrue("constants not defined:\n" + gen, gen.contains("E_Color* E_Color::GREEN = nullptr;"));
        assertTrue("constants not initialized:\n" + gen, gen.contains("new E_Color(20)"));
        String header = new String(Files.readAllBytes(out.resolve("ep/E_Color.h")), StandardCharsets.UTF_8);
        assertTrue("ordinal missing:\n" + header, header.contains("int ordinal()"));

        CppTestSupport.compile(out, compiler, "src/ep/E.cpp", MAIN, "en_test")
                .assertOk("en");
        CppTestSupport.runMerged(out.resolve("en_test")).assertOk("en");
    }
}
