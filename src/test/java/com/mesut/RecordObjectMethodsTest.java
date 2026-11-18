package com.mesut;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertTrue;

/** Verifies the auto-generated toString/equals/hashCode on records. */
public class RecordObjectMethodsTest {

    private static final String JAVA =
            "package rp;\n" +
            "public class R {\n" +
            "    public record Point(int x, int y) {}\n" +
            "    public record Named(String name, Point p) {}\n" +
            "}\n";

    private static final String MAIN =
            "#include \"common.h\"\n" +
            "#include \"rp/R_Point.h\"\n" +
            "#include \"rp/R_Named.h\"\n" +
            "#include <cstdio>\n" +
            "#include <string>\n" +
            "int main(){\n" +
            "    rp::R_Point p(1,2), q(1,2), r(3,4);\n" +
            "    rp::R_Named n(new std::string(\"hi\"), &p);\n" +
            "    std::string ts = *p.toString();\n" +
            "    std::string nts = *n.toString();\n" +
            "    java::lang::Object* op = &p, *oq = &q, *oz = &r;\n" +
            "    std::string vts = *op->toString();\n" +
            "    printf(\"ts=%s eq=%d neq=%d h=%d n=%s vts=%s\\n\", ts.c_str(), p.equals(&q), p.equals(&r), p.hashCode(), nts.c_str(), vts.c_str());\n" +
            "    return (ts==\"R_Point[x=1, y=2]\" && p.equals(&q) && !p.equals(&r)\n" +
            "            && p.hashCode()==q.hashCode()\n" +
            "            && nts.find(\"name=hi\")!=std::string::npos\n" +
            "            && nts.find(\"p=R_Point[x=1, y=2]\")!=std::string::npos\n" +
            "            && vts==ts && op->equals(oq) && !op->equals(oz) && op->hashCode()==q.hashCode()) ? 0 : 1;\n" +
            "}\n";

    @Test
    public void recordObjectMethods() throws Exception {
        Path compiler = CppTestSupport.compiler();

        Path out = CppTestSupport.convert(JAVA, "rp", "R", "rom");

        String objectHeader = new String(Files.readAllBytes(
                out.resolve("lib/java/lang/Object.h")), StandardCharsets.UTF_8);
        assertTrue("Object stub missing virtual toString:\n" + objectHeader,
                objectHeader.contains("virtual std::string* toString()"));
        assertTrue("Object stub missing virtual equals:\n" + objectHeader,
                objectHeader.contains("virtual bool equals(Object* o)"));
        assertTrue("Object stub missing virtual hashCode:\n" + objectHeader,
                objectHeader.contains("virtual int hashCode()"));

        CppTestSupport.compile(out, compiler, "src/rp/R.cpp", MAIN, "rom_test")
                .assertOk("rom");
        CppTestSupport.runMerged(out.resolve("rom_test")).assertOk("rom");
    }
}
