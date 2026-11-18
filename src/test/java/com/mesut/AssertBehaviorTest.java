package com.mesut;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertTrue;

/** Verifies that Java assert is emitted as a C++ assert with the right include. */
public class AssertBehaviorTest {

    private static final String JAVA =
            "package a;\n" +
            "public class A {\n" +
            "    int test(int x) {\n" +
            "        assert x > 0;\n" +
            "        return x;\n" +
            "    }\n" +
            "}\n";

    private static final String MAIN =
            "#include \"common.h\"\n" +
            "#include \"a/A.h\"\n" +
            "#include <cstdio>\n" +
            "int main(){ a::A o; printf(\"r=%d\\n\", o.test(5)); return o.test(5)==5?0:1; }\n";

    @Test
    public void assertIsEmitted() throws Exception {
        Path compiler = CppTestSupport.compiler();

        Path out = CppTestSupport.convert(JAVA, "a", "A", "assert");

        String cpp = new String(Files.readAllBytes(out.resolve("src/a/A.cpp")), StandardCharsets.UTF_8);
        assertTrue("assert not emitted: " + cpp, cpp.contains("assert(x>0)"));

        CppTestSupport.compile(out, compiler, "src/a/A.cpp", MAIN, "assert_test")
                .assertOk("assert");
        CppTestSupport.runMerged(out.resolve("assert_test")).assertOk("assert");
    }
}
