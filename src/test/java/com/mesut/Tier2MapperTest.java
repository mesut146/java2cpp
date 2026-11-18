package com.mesut;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertTrue;

/** Tier 2 stdlib: Math, boxed primitives, StringBuilder, Objects, Optional, Random, UUID. */
public class Tier2MapperTest {

    private static final String JAVA = """
            package t2;
            import java.util.*;
            public class T2B {
                public static int main(String[] a) {
                    int r = 0;
                    if (Math.sqrt(16) == 4) r += 1;
                    if (Math.abs(-5) == 5) r += 2;
                    if (Math.round(2.6) == 3) r += 4;
                    double rr = Math.random();
                    if (rr >= 0 && rr < 1) r += 8;

                    StringBuilder sb = new StringBuilder("ab");
                    sb.append("c").insert(0, "x");
                    if (sb.length() == 4) r += 16;
                    if (sb.toString().equals("xabc")) r += 32;
                    sb.reverse();
                    if (sb.toString().equals("cbax")) r += 64;
                    StringBuilder sb2 = new StringBuilder("hello");
                    sb2.delete(1, 3);
                    if (sb2.toString().equals("hlo")) r += 128;

                    if (Objects.isNull(null)) r += 256;
                    if (Objects.nonNull("x")) r += 512;
                    if (Objects.requireNonNull("y").equals("y")) r += 1024;
                    if (Objects.toString("z").equals("z")) r += 2048;

                    Optional<String> o = Optional.of("v");
                    if (o.isPresent() && o.get().equals("v")) r += 4096;

                    Random rnd = new Random();
                    int n = rnd.nextInt(10);
                    if (n >= 0 && n < 10) r += 8192;
                    if (rnd.nextDouble() >= 0 && rnd.nextDouble() < 1) r += 16384;

                    if (Character.isDigit('7')) r += 32768;
                    if (Character.toUpperCase('a') == 'A') r += 65536;
                    if (Character.toString('q').equals("q")) r += 131072;

                    if (Double.parseDouble("2.5") == 2.5) r += 262144;
                    if (Float.parseFloat("1.5") == 1.5f) r += 524288;
                    if (Long.parseLong("123") == 123L) r += 1048576;
                    if (Short.parseShort("7") == 7) r += 2097152;
                    if (UUID.randomUUID().toString().length() == 36) r += 4194304;
                    if (Math.pow(2, 3) == 8) r += 8388608;
                    if (Math.max(3, 7) == 7) r += 16777216;
                    if (Math.PI > 3.14 && Math.PI < 3.15) r += 33554432;
                    return r;
                }
            }
            """;

    private static final String MAIN = """
            #include "common.h"
            #include "t2/T2B.h"
            #include <cstdio>
            int main(){
                int r = t2::T2B::main(nullptr);
                printf("r=%d\\n", r);
                return r == 67108863 ? 0 : 1;
            }
            """;

    @Test
    public void tier2Mappings() throws Exception {
        Path compiler = CppTestSupport.compiler();

        Path out = CppTestSupport.convert(JAVA, "t2", "T2B", "t2");

        String gen = new String(Files.readAllBytes(out.resolve("src/t2/T2B.cpp")), StandardCharsets.UTF_8);
        assertTrue("Math.pow not mapped:\n" + gen, gen.contains("std::pow("));
        assertTrue("Math.PI not mapped:\n" + gen, gen.contains("3.141592653589793"));
        assertTrue("StringBuilder not mapped:\n" + gen, gen.contains("new CppHelper::StringBuilder("));
        assertTrue("Objects not mapped:\n" + gen, gen.contains("CppHelper::objToString("));
        assertTrue("Optional not mapped:\n" + gen, gen.contains("new std::optional("));
        assertTrue("Random not mapped:\n" + gen, gen.contains("new CppHelper::Random()"));
        assertTrue("Character not mapped:\n" + gen, gen.contains("std::iswdigit("));
        assertTrue("UUID not mapped:\n" + gen, gen.contains("CppHelper::randomUUID()"));

        CppTestSupport.compile(out, compiler, "src/t2/T2B.cpp", MAIN, "t2_test")
                .assertOk("t2");
        CppTestSupport.runMerged(out.resolve("t2_test")).assertOk("t2");
    }
}
