package com.mesut;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertTrue;

/** String/enum switches must honor break and fall-through; loop breaks stay in the loop. */
public class SwitchBreakTest {

    private static final String JAVA = """
            package s;
            public class S {
                enum Color { RED, GREEN, BLUE }
                public static int main(String[] a) {
                    int r = 0;
                    String x = "b";
                    switch (x) {
                        case "a": r += 1; break;
                        case "b": r += 2;
                        case "c": r += 4; break;
                        default: r += 8;
                    }
                    Color c = Color.GREEN;
                    switch (c) {
                        case RED: r += 16; break;
                        case GREEN: r += 32;
                        case BLUE: r += 64; break;
                    }
                    switch (x) {
                        case "b":
                            for (int i = 0; i < 3; i++) { break; }
                            r += 4;
                            break;
                        default: r += 8;
                    }
                    return r;
                }
            }
            """;

    private static final String MAIN = """
            #include "common.h"
            #include "s/S.h"
            #include <cstdio>
            int main(){
                int r = s::S::main(nullptr);
                printf("r=%d\\n", r);
                return r == 106 ? 0 : 1;
            }
            """;

    @Test
    public void switchBreakAndFallThrough() throws Exception {
        Path compiler = CppTestSupport.compiler();

        Path out = CppTestSupport.convert(JAVA, "s", "S", "swbr");

        String gen = new String(Files.readAllBytes(out.resolve("src/s/S.cpp")), StandardCharsets.UTF_8);
        assertTrue("goto dispatch missing:\n" + gen, gen.contains("goto sw0_case_0"));
        assertTrue("break not redirected:\n" + gen, gen.contains("goto sw0_end"));
        assertTrue("for loop malformed:\n" + gen, gen.contains("auto i = 0;i<3;i++"));

        CppTestSupport.compile(out, compiler, "src/s/S.cpp", MAIN, "swbr_test")
                .assertOk("swbr");
        CppTestSupport.runMerged(out.resolve("swbr_test")).assertOk("swbr");
    }
}
