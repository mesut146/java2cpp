package com.mesut;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertTrue;

/** Verifies q.new Inner() passes q as the enclosing instance, even in a static method. */
public class QualifiedInnerCreationTest {

    private static final String JAVA =
            "package qt;\n" +
            "public class Q {\n" +
            "    int x;\n" +
            "    Q(int x) { this.x = x; }\n" +
            "    class Inner {\n" +
            "        int get() { return Q.this.x; }\n" +
            "    }\n" +
            "    public static int main(String[] a) {\n" +
            "        Q q = new Q(5);\n" +
            "        return q.new Inner().get();\n" +
            "    }\n" +
            "}\n";

    private static final String MAIN =
            "#include \"common.h\"\n" +
            "#include \"qt/Q.h\"\n" +
            "#include <cstdio>\n" +
            "int main(){\n" +
            "    int r = qt::Q::main(nullptr);\n" +
            "    printf(\"r=%d\\n\", r);\n" +
            "    return r == 5 ? 0 : 1;\n" +
            "}\n";

    @Test
    public void qualifiedInnerUsesEnclosingInstance() throws Exception {
        Path compiler = CppTestSupport.compiler();

        Path out = CppTestSupport.convert(JAVA, "qt", "Q", "qi");

        String gen = new String(Files.readAllBytes(out.resolve("src/qt/Q.cpp")), StandardCharsets.UTF_8);
        assertTrue("wrong enclosing instance:\n" + gen, gen.contains("new Q_Inner(q)"));
        assertTrue("receiver needs parentheses:\n" + gen, gen.contains("(new Q_Inner(q))->get()"));

        CppTestSupport.compile(out, compiler, "src/qt/Q.cpp", MAIN, "qi_test")
                .assertOk("qi");
        CppTestSupport.runMerged(out.resolve("qi_test")).assertOk("qi");
    }
}
