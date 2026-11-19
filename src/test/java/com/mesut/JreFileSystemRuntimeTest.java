package com.mesut;

import org.junit.Test;

import javax.tools.ToolProvider;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;

/** File/Path/Files in -no-mappers mode must behave like the JDK implementation. */
public class JreFileSystemRuntimeTest {

    private static final String TEMPLATE = """
            package io;
            import java.io.File;
            import java.nio.file.*;
            import java.util.List;
            public class I {
                static int test() throws Exception {
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

    private static final String RUNNER = """
            package io;
            public class Runner {
                public static void main(String[] a) throws Exception {
                    System.out.println(I.test());
                }
            }
            """;

    private static final String MAIN = """
            #include "common.h"
            #include "io/I.h"
            #include <cstdio>
            int main(){
                printf("%d\\n", io::I::test());
                return 0;
            }
            """;

    @Test
    public void filesystemApiMatchesJvm() throws Exception {
        Path jvmBase = Files.createTempDirectory("j2cpp-jrefs-jvm");
        String jvmProgram = TEMPLATE.replace("__BASE__", jvmBase.toString());
        Path javaSrc = Files.createTempDirectory("j2cpp-jrefs-javasrc");
        Files.createDirectories(javaSrc.resolve("io"));
        Path file = javaSrc.resolve("io/I.java");
        Files.write(file, jvmProgram.getBytes(StandardCharsets.UTF_8));
        Path runner = javaSrc.resolve("io/Runner.java");
        Files.write(runner, RUNNER.getBytes(StandardCharsets.UTF_8));
        Path classes = Files.createTempDirectory("j2cpp-jrefs-classes");
        assertEquals("javac failed", 0, ToolProvider.getSystemJavaCompiler()
                .run(null, null, null, "-d", classes.toString(), file.toString(), runner.toString()));
        Process jp = new ProcessBuilder("java", "-cp", classes.toString(), "io.Runner")
                .redirectErrorStream(true).start();
        String expected = new String(jp.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();

        Path cppBase = Files.createTempDirectory("j2cpp-jrefs-cpp");
        String cppProgram = TEMPLATE.replace("__BASE__", cppBase.toString());
        Path compiler = CppTestSupport.compiler();
        Path out = CppTestSupport.convertNoMappers(cppProgram, "io", "I", "jrefs");
        CppTestSupport.compile(out, compiler, "src/io/I.cpp", MAIN, "jrefs_test")
                .assertOk("JRE filesystem test");
        CppTestSupport.RunResult actual = CppTestSupport.runMerged(out.resolve("jrefs_test"));
        actual.assertOk("JRE filesystem test");
        assertEquals("C++ filesystem output must match JVM:\n" + actual.output,
                expected, actual.output.trim());
    }
}
