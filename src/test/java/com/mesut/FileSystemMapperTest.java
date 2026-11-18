package com.mesut;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertTrue;

/** Tier 3b: java.io.File / Path / Files map to std::filesystem and really touch disk. */
public class FileSystemMapperTest {

    private static final String JAVA_TEMPLATE = """
            package io;
            import java.io.File;
            import java.nio.file.*;
            import java.util.List;
            public class I {
                public static int main(String[] a) throws Exception {
                    int r = 0;
                    String base = "__BASE__";
                    Path p = Paths.get(base + "/test.txt");
                    Files.writeString(p, "hello\\nworld");
                    if (Files.exists(p)) r += 1;
                    String s = Files.readString(p);
                    if (s.equals("hello\\nworld")) r += 2;
                    if (Files.size(p) == 11) r += 4;
                    List<String> lines = Files.readAllLines(p);
                    if (lines.size() == 2 && lines.get(1).equals("world")) r += 8;
                    File f = new File(base + "/test.txt");
                    if (f.exists() && !f.isDirectory()) r += 16;
                    if (f.getName().equals("test.txt")) r += 32;
                    if (f.getParent().equals(base)) r += 64;
                    File target = new File(base + "/renamed.txt");
                    if (f.renameTo(target) && target.exists()) r += 128;
                    if (target.delete() && !target.exists()) r += 256;
                    Path dir = Paths.get(base + "/dir");
                    Files.createDirectories(dir);
                    Files.writeString(Paths.get(base + "/dir/child.txt"), "x");
                    File d = new File(base + "/dir");
                    if (d.isDirectory()) r += 512;
                    File[] kids = d.listFiles();
                    if (kids.length > 0) r += 1024;
                    Files.deleteIfExists(Paths.get(base + "/dir/child.txt"));
                    Files.deleteIfExists(dir);
                    return r;
                }
            }
            """;

    private static final String MAIN = """
            #include "common.h"
            #include "io/I.h"
            #include <cstdio>
            int main(){
                int r = io::I::main(nullptr);
                printf("r=%d\\n", r);
                return r == 2047 ? 0 : 1;
            }
            """;

    @Test
    public void fileSystemMappings() throws Exception {
        Path compiler = CppTestSupport.compiler();

        Path base = Files.createTempDirectory("j2cpp-fs-base");
        String java = JAVA_TEMPLATE.replace("__BASE__", base.toString());

        Path out = CppTestSupport.convert(java, "io", "I", "fs");

        String gen = new String(Files.readAllBytes(out.resolve("src/io/I.cpp")), StandardCharsets.UTF_8);
        assertTrue("File not mapped:\n" + gen, gen.contains("CppHelper::fsPath("));
        assertTrue("Files.readString not mapped:\n" + gen, gen.contains("CppHelper::fsReadString("));
        assertTrue("File.getName not mapped:\n" + gen, gen.contains("CppHelper::fsFileName("));
        assertTrue("File.renameTo not mapped:\n" + gen, gen.contains("CppHelper::fsRename("));
        assertTrue("listFiles not mapped:\n" + gen, gen.contains("CppHelper::fsList("));

        CppTestSupport.compile(out, compiler, "src/io/I.cpp", MAIN, "fs_test")
                .assertOk("fs");
        CppTestSupport.runMerged(out.resolve("fs_test")).assertOk("fs");
    }
}
