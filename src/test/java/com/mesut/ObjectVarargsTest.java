package com.mesut;

import org.junit.Test;

import java.nio.file.Path;

/** Mixed Java values passed to an Object... parameter must be boxed for C++. */
public class ObjectVarargsTest {

    private static final String JAVA = """
            package ov;
            public class V {
                static String show(Object... xs) { return "" + xs.length; }
                public static int main(String[] a) {
                    String s = show("x", 5, true, null);
                    return s.equals("4") ? 1 : 0;
                }
            }
            """;

    private static final String MAIN = """
            #include "common.h"
            #include "ov/V.h"
            #include <cstdio>
            int main(){
                int r = ov::V::main(nullptr);
                printf("r=%d\\n", r);
                return r == 1 ? 0 : 1;
            }
            """;

    @Test
    public void mixedVarargsAreBoxedToObject() throws Exception {
        Path compiler = CppTestSupport.compiler();
        Path out = CppTestSupport.convert(JAVA, "ov", "V", "object-varargs");

        String cpp = CppTestSupport.generated(out, "src/ov/V.cpp");
        CppTestSupport.assertContains(cpp, "j2cpp::boxArg(new std::string(\"x\"))");
        CppTestSupport.assertContains(cpp, "j2cpp::boxArg(nullptr)");
        CppTestSupport.assertContains(CppTestSupport.log(out), "argument boxed for Object varargs");

        CppTestSupport.compile(out, compiler, "src/ov/V.cpp", MAIN, "object_varargs_test")
                .assertOk("object varargs test");
        CppTestSupport.runMerged(out.resolve("object_varargs_test")).assertOk("object varargs test");
    }
}
