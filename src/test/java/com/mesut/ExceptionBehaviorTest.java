package com.mesut;

import org.junit.Test;

import java.nio.file.Path;


/** Verifies multi-catch and rethrow behavior of generated exception handling. */
public class ExceptionBehaviorTest {

    private static final String JAVA =
            "package e;\n" +
            "public class Ex {\n" +
            "    static int log = 0;\n" +
            "    static int test(int mode) {\n" +
            "        try {\n" +
            "            if (mode == 1) throw new IllegalStateException(\"a\");\n" +
            "            return 0;\n" +
            "        } catch (IllegalStateException | IllegalArgumentException ex) {\n" +
            "            log = 1;\n" +
            "            return 10;\n" +
            "        }\n" +
            "    }\n" +
            "    static int rethrow() {\n" +
            "        try { throw new IllegalStateException(\"x\"); }\n" +
            "        catch (IllegalStateException ex) { log = 2; throw ex; }\n" +
            "    }\n" +
            "}\n";

    private static final String MAIN =
            "#include \"common.h\"\n" +
            "#include \"e/Ex.h\"\n" +
            "#include \"CppHelper.h\"\n" +
            "#include <cstdio>\n" +
            "int main(){\n" +
            "    e::Ex::log=0; int a=e::Ex::test(0);\n" +
            "    e::Ex::log=0; int b=e::Ex::test(1);\n" +
            "    bool caught=false; try { e::Ex::rethrow(); } catch (CppHelper::Throwable&) { caught=true; }\n" +
            "    printf(\"a=%d b=%d log=%d caught=%d\\n\", a, b, e::Ex::log, caught);\n" +
            "    return (a==0 && b==10 && e::Ex::log==2 && caught) ? 0 : 1;\n" +
            "}\n";

    @Test
    public void multiCatchAndRethrow() throws Exception {
        Path compiler = CppTestSupport.compiler();

        Path out = CppTestSupport.convert(JAVA, "e", "Ex", "ex");

        CppTestSupport.compile(out, compiler, "src/e/Ex.cpp", MAIN, "ex_test")
                .assertOk("ex");
        CppTestSupport.runMerged(out.resolve("ex_test")).assertOk("ex");
    }
}
