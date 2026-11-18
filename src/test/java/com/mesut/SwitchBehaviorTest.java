package com.mesut;

import org.junit.Test;

import java.nio.file.Path;


/** Verifies that generated switch statements behave like Java switches. */
public class SwitchBehaviorTest {

    private static final String JAVA =
            "package s;\n" +
            "public class S {\n" +
            "    static int last = 0;\n" +
            "    int f(int x) {\n" +
            "        switch (x) {\n" +
            "            case 1: last = 1; return 10;\n" +
            "            case 2: last = 2; return 20;\n" +
            "            default: last = 3; return 30;\n" +
            "        }\n" +
            "    }\n" +
            "}\n";

    private static final String MAIN =
            "#include \"common.h\"\n" +
            "#include \"s/S.h\"\n" +
            "#include <cstdio>\n" +
            "int main() {\n" +
            "    s::S obj; int fails = 0;\n" +
            "    s::S::last=0; int a=obj.f(1); if(a!=10||s::S::last!=1){printf(\"f1=%d last=%d\\n\",a,s::S::last);fails++;}\n" +
            "    s::S::last=0; int b=obj.f(2); if(b!=20||s::S::last!=2){printf(\"f2=%d last=%d\\n\",b,s::S::last);fails++;}\n" +
            "    s::S::last=0; int c=obj.f(9); if(c!=30||s::S::last!=3){printf(\"f9=%d last=%d\\n\",c,s::S::last);fails++;}\n" +
            "    printf(\"fails=%d\\n\", fails);\n" +
            "    return fails;\n" +
            "}\n";

    @Test
    public void switchBehavior() throws Exception {
        Path compiler = CppTestSupport.compiler();

        Path out = CppTestSupport.convert(JAVA, "s", "S", "sw");

        CppTestSupport.compile(out, compiler, "src/s/S.cpp", MAIN, "sw_test")
                .assertOk("sw");
        CppTestSupport.runMerged(out.resolve("sw_test")).assertOk("sw");
    }
}
