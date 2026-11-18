package com.mesut;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertTrue;

/** Tier 3c: BufferedReader/PrintWriter/Scanner mappers and try-with-resources. */
public class StreamMapperTest {

    private static final String JAVA_TEMPLATE = """
            package io;
            import java.io.*;
            import java.nio.file.*;
            import java.util.Scanner;
            public class T {
                public static int main(String[] a) throws Exception {
                    int r = 0;
                    String base = "__BASE__";
                    Path in = Paths.get(base + "/in.txt");
                    Files.writeString(in, "hello\\nworld");
                    try (BufferedReader br = new BufferedReader(new FileReader(base + "/in.txt"))) {
                        if ("hello".equals(br.readLine())) r += 1;
                        if ("world".equals(br.readLine())) r += 2;
                        if (br.readLine() == null) r += 4;
                    }
                    try (PrintWriter pw = new PrintWriter(base + "/out.txt")) {
                        pw.println("done");
                    }
                    String out = Files.readString(Paths.get(base + "/out.txt"));
                    if (out.equals("done\\n")) r += 8;
                    Scanner sc = new Scanner("7 eight");
                    if (sc.nextInt() == 7) r += 16;
                    if ("eight".equals(sc.next())) r += 32;
                    return r;
                }
            }
            """;

    private static final String MAIN = """
            #include "common.h"
            #include "io/T.h"
            #include <cstdio>
            int main(){
                int r = io::T::main(nullptr);
                printf("r=%d\\n", r);
                return r == 63 ? 0 : 1;
            }
            """;

    @Test
    public void streamsAndTryWithResources() throws Exception {
        Path compiler = CppTestSupport.compiler();

        Path base = Files.createTempDirectory("j2cpp-stream-base");
        String java = JAVA_TEMPLATE.replace("__BASE__", base.toString());

        Path out = CppTestSupport.convert(java, "io", "T", "stream");

        String gen = new String(Files.readAllBytes(out.resolve("src/io/T.cpp")), StandardCharsets.UTF_8);
        assertTrue("BufferedReader not mapped:\n" + gen, gen.contains("new CppHelper::BufferedReader("));
        assertTrue("PrintWriter not mapped:\n" + gen, gen.contains("new CppHelper::PrintWriter("));
        assertTrue("Scanner not mapped:\n" + gen, gen.contains("new CppHelper::Scanner("));
        assertTrue("resource not declared:\n" + gen, gen.contains("auto br = new CppHelper::BufferedReader("));
        assertTrue("close guard missing:\n" + gen, gen.contains("CppHelper::ScopeGuard"));

        CppTestSupport.compile(out, compiler, "src/io/T.cpp", MAIN, "stream_test")
                .assertOk("stream");
        CppTestSupport.runMerged(out.resolve("stream_test")).assertOk("stream");
    }
}
