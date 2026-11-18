package com.mesut;

import org.junit.Test;

import java.nio.file.Path;

/** Small mapper API gaps: split limits, copyOfRange, ofNullable, valueOf overloads. */
public class Tier4MapperTest {

    private static final String JAVA = """
            package t4;
            import java.util.*;
            public class T4 {
                public static int main(String[] a) {
                    String s = "a,b,c,";
                    int n = s.split(",", 2).length + s.split(",", 0).length
                            + s.split(",", -1).length;
                    int[] xs = new int[]{1, 2, 3, 4};
                    int[] ys = Arrays.copyOfRange(xs, 1, 4);
                    int m = ys.length + ys[0] + ys[2];
                    Optional<Integer> o = Optional.ofNullable(7);
                    int k = (o.isPresent() ? 1 : 0) + o.get();
                    String b = String.valueOf(true) + String.valueOf(42) + String.valueOf(2.5);
                    return n + m + k + b.length();
                }
            }
            """;

    private static final String MAIN = """
            #include "common.h"
            #include "t4/T4.h"
            #include <cstdio>
            int main(){
                int r = t4::T4::main(nullptr);
                printf("r=%d\\n", r);
                return r == 35 ? 0 : 1;
            }
            """;

    @Test
    public void smallApiGapsAreMappedAndBehave() throws Exception {
        Path compiler = CppTestSupport.compiler();
        Path out = CppTestSupport.convert(JAVA, "t4", "T4", "tier4");

        String cpp = CppTestSupport.generated(out, "src/t4/T4.cpp");
        CppTestSupport.assertContains(cpp, "CppHelper::split(s, new std::string(\",\"), 2)");
        CppTestSupport.assertContains(cpp, "CppHelper::split(s, new std::string(\",\"), -1)");
        CppTestSupport.assertContains(cpp, "CppHelper::copyOfRange(xs, 1, 4)");
        CppTestSupport.assertContains(cpp, "new std::optional<decltype(7)>(7)");
        CppTestSupport.assertContains(cpp, "new std::string(true ? \"true\" : \"false\")");
        CppTestSupport.assertContains(cpp, "CppHelper::toStringPtr(2.5)");

        CppTestSupport.compile(out, compiler, "src/t4/T4.cpp", MAIN, "tier4_test")
                .assertOk("tier 4 mapper test");
        CppTestSupport.runMerged(out.resolve("tier4_test")).assertOk("tier 4 mapper test");
    }
}
