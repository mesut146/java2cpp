package com.mesut;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertTrue;

/** A Number loop variable over List<Integer> must box the mapped primitive element. */
public class NumberBoxingTest {

    private static final String JAVA = """
            package n;
            import java.util.*;
            public class N {
                public static int sum(List<? extends Number> xs) {
                    int r = 0;
                    for (Number x : xs) r += x.intValue();
                    return r;
                }
                public static int main(String[] a) {
                    List<Integer> l = new ArrayList<Integer>();
                    l.add(1); l.add(2); l.add(3);
                    return sum(l);
                }
            }
            """;

    private static final String MAIN = """
            #include "common.h"
            #include "n/N.h"
            #include <cstdio>
            int main(){
                int r = n::N::main(nullptr);
                printf("r=%d\\n", r);
                return r == 6 ? 0 : 1;
            }
            """;

    @Test
    public void numberLoopVariableBoxesElements() throws Exception {
        Path compiler = CppTestSupport.compiler();

        Path out = CppTestSupport.convert(JAVA, "n", "N", "num");

        String gen = new String(Files.readAllBytes(out.resolve("src/n/N.cpp")), StandardCharsets.UTF_8);
        assertTrue("Number not mapped:\n" + gen, gen.contains("CppHelper::Number* x"));
        assertTrue("element not boxed:\n" + gen, gen.contains("CppHelper::boxNumber(*it)"));

        CppTestSupport.compile(out, compiler, "src/n/N.cpp", MAIN, "num_test")
                .assertOk("num");
        CppTestSupport.runMerged(out.resolve("num_test")).assertOk("num");
    }
}
