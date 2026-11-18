package com.mesut;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertTrue;

/** Verifies that an override of a method declared several levels up dispatches virtually. */
public class VirtualDispatchTest {

    private static final String JAVA =
            "package vp;\n" +
            "public class V {\n" +
            "    public static class A { public String who() { return \"A\"; } }\n" +
            "    public static class B extends A { }\n" +
            "    public static class C extends B {\n" +
            "        @Override public String who() { return \"C\"; }\n" +
            "    }\n" +
            "}\n";

    private static final String MAIN =
            "#include \"common.h\"\n" +
            "#include \"vp/V_C.h\"\n" +
            "#include <cstdio>\n" +
            "#include <string>\n" +
            "int main(){\n" +
            "    vp::V_C c;\n" +
            "    vp::V_A* a = &c;\n" +
            "    vp::V_B* b = &c;\n" +
            "    std::string s1 = *a->who(), s2 = *b->who(), s3 = *c.who();\n" +
            "    printf(\"%s %s %s\\n\", s1.c_str(), s2.c_str(), s3.c_str());\n" +
            "    return (s1==\"C\" && s2==\"C\" && s3==\"C\") ? 0 : 1;\n" +
            "}\n";

    @Test
    public void deepOverrideDispatchesVirtually() throws Exception {
        Path compiler = CppTestSupport.compiler();

        Path out = CppTestSupport.convert(JAVA, "vp", "V", "vd");

        String base = new String(Files.readAllBytes(out.resolve("vp/V_A.h")), StandardCharsets.UTF_8);
        String sub = new String(Files.readAllBytes(out.resolve("vp/V_C.h")), StandardCharsets.UTF_8);
        assertTrue("base method not virtual:\n" + base, base.contains("virtual std::string* who()"));
        assertTrue("override not marked:\n" + sub, sub.contains("std::string* who() override"));

        CppTestSupport.compile(out, compiler, "src/vp/V.cpp", MAIN, "vd_test")
                .assertOk("vd");
        CppTestSupport.runMerged(out.resolve("vd_test")).assertOk("vd");
    }
}
