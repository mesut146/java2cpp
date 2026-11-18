package com.mesut;

import org.junit.Test;

import java.nio.file.Path;

/**
 * Verifies that generated try/finally has Java semantics by compiling and running
 * the generated C++: finally runs on normal completion, early return and throw,
 * and a return in finally overrides the try result or a pending exception.
 */
public class TryFinallyBehaviorTest {

    private static final String JAVA =
            "package t;\n" +
            "public class F {\n" +
            "    static int log = 0;\n" +
            "    int normal() { try { log = log * 10 + 1; } finally { log = log * 10 + 9; } return log; }\n" +
            "    int earlyReturn() { try { log = log * 10 + 1; return 5; } finally { log = log * 10 + 9; } }\n" +
            "    int finallyWins() { try { log = log * 10 + 1; return 1; } finally { log = log * 10 + 9; return 2; } }\n" +
            "    int throwing() { try { log = log * 10 + 1; throw new RuntimeException(\"x\"); } finally { log = log * 10 + 9; } }\n" +
            "    int throwFinallyWins() { try { log = log * 10 + 1; throw new RuntimeException(\"x\"); } finally { log = log * 10 + 9; return 7; } }\n" +
            "}\n";

    private static final String MAIN =
            "#include \"common.h\"\n" +
            "#include \"CppHelper.h\"\n" +
            "#include \"t/F.h\"\n" +
            "#include <cstdio>\n" +
            "int main() {\n" +
            "    t::F f; int fails = 0;\n" +
            "    t::F::log=0; int a=f.normal();\n" +
            "    if(a!=19||t::F::log!=19) { printf(\"normal=%d log=%d\\n\", a, t::F::log); fails++; }\n" +
            "    t::F::log=0; int b=f.earlyReturn();\n" +
            "    if(b!=5||t::F::log!=19) { printf(\"early=%d log=%d\\n\", b, t::F::log); fails++; }\n" +
            "    t::F::log=0; int c=f.finallyWins();\n" +
            "    if(c!=2||t::F::log!=19) { printf(\"wins=%d log=%d\\n\", c, t::F::log); fails++; }\n" +
            "    t::F::log=0; bool caught=false;\n" +
            "    try { f.throwing(); } catch (CppHelper::Throwable&) { caught=true; }\n" +
            "    if(!caught||t::F::log!=19) { printf(\"throwing caught=%d log=%d\\n\", caught, t::F::log); fails++; }\n" +
            "    t::F::log=0; int d=f.throwFinallyWins();\n" +
            "    if(d!=7||t::F::log!=19) { printf(\"throwWins=%d log=%d\\n\", d, t::F::log); fails++; }\n" +
            "    printf(\"fails=%d\\n\", fails);\n" +
            "    return fails;\n" +
            "}\n";

    @Test
    public void finallySemantics() throws Exception {
        Path compiler = CppTestSupport.compiler();
        Path out = CppTestSupport.convert(JAVA, "t", "F", "fin");

        CppTestSupport.compile(out, compiler, "src/t/F.cpp", MAIN, "fin_test")
                .assertOk("try/finally test");
        CppTestSupport.runMerged(out.resolve("fin_test")).assertOk("try/finally test");
    }
}
