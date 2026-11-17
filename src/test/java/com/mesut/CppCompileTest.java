package com.mesut;

import com.mesut.j2cpp.Cmd;
import org.junit.Assume;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Compiles generated C++ to verify that mapper includes, the generated header and
 * the runtime helper header are wired up correctly.
 */
public class CppCompileTest {

    private Path compiler() {
        return Arrays.stream(System.getenv("PATH").split(":"))
                .map(Path::of)
                .map(dir -> dir.resolve("g++"))
                .filter(Files::isExecutable)
                .findFirst()
                .orElse(null);
    }

    private Path generateAndCompile(String className, String source) throws Exception {
        Path compiler = compiler();
        Assume.assumeTrue("g++ is not available", compiler != null);

        Path src = Files.createTempDirectory("j2cpp-compile-src");
        Files.createDirectories(src.resolve("demo"));
        Files.write(src.resolve("demo/" + className + ".java"), source.getBytes(StandardCharsets.UTF_8));
        Path out = Files.createTempDirectory("j2cpp-compile-out");
        Cmd.main(new String[]{"-src", src.toString(), "-out", out.toString()});

        Path generated = out.resolve("src/demo/" + className + ".cpp");
        compile(compiler, out, generated);
        return generated;
    }

    private void compile(Path compiler, Path out, Path generated) throws Exception {
        Process process = new ProcessBuilder(
                compiler.toString(), "-std=c++17", "-fsyntax-only",
                "-I" + Path.of("cppRt").toAbsolutePath(), "-I" + out, generated.toString())
                .redirectErrorStream(true)
                .start();
        String log = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals("generated C++ did not compile:\n" + log,
                0, process.waitFor(60, TimeUnit.SECONDS) ? process.exitValue() : -1);
    }

    @Test
    public void generatedSourceCompiles() throws Exception {
        generateAndCompile("HelperUse",
                "package demo;\n" +
                        "public class HelperUse {\n" +
                        "    public String test(String s) {\n" +
                        "        String a = s.toUpperCase();\n" +
                        "        String b = s.replace(\"x\", \"y\");\n" +
                        "        String[] parts = s.split(\",\");\n" +
                        "        return b;\n" +
                        "    }\n" +
                        "}\n");
    }

    @Test
    public void stringConcatAndStaticCallCompile() throws Exception {
        Path generated = generateAndCompile("StrFeatures",
                "package demo;\n" +
                        "public class StrFeatures {\n" +
                        "    public String test(String s, int n) {\n" +
                        "        String joined = s + \"x\" + n;\n" +
                        "        String formatted = String.format(\"%s\", s);\n" +
                        "        return joined;\n" +
                        "    }\n" +
                        "}\n");
        String cpp = new String(Files.readAllBytes(generated), StandardCharsets.UTF_8);
        assertTrue("string concat was not rewritten: " + cpp, cpp.contains("CppHelper::concat"));
        assertTrue("static call was not mapped: " + cpp, cpp.contains("CppHelper::format"));
    }

    private Path convertSamples() throws Exception {
        Path src = Path.of("src/test/resources/samples").toAbsolutePath();
        Path out = Files.createTempDirectory("j2cpp-samples-out");
        Cmd.main(new String[]{"-src", src.toString(), "-out", out.toString()});
        return out;
    }

    @Test
    public void genericSampleCompiles() throws Exception {
        Path compiler = compiler();
        Assume.assumeTrue("g++ is not available", compiler != null);

        Path out = convertSamples();
        Path header = out.resolve("base/Generic.h");
        Path generated = out.resolve("src/base/Generic.cpp");
        String h = new String(Files.readAllBytes(header), StandardCharsets.UTF_8);
        String cpp = new String(Files.readAllBytes(generated), StandardCharsets.UTF_8);
        assertTrue("class template missing: " + h, h.contains("template <class T, class V>"));
        assertTrue("type arguments were dropped: " + cpp, cpp.contains("std::vector<"));
        assertTrue("dependent base call not qualified: " + cpp, cpp.contains("this->add("));
        compile(compiler, out, generated);
    }

    @Test
    public void arrayCreationCompiles() throws Exception {
        Path compiler = compiler();
        Assume.assumeTrue("g++ is not available", compiler != null);

        Path src = Files.createTempDirectory("j2cpp-arr-src");
        Files.createDirectories(src.resolve("p"));
        Files.write(src.resolve("p/A.java"),
                ("package p;\n" +
                        "public class A {\n" +
                        "    void m() {\n" +
                        "        int[] xs = new int[5];\n" +
                        "        int[][] ys = new int[2][3];\n" +
                        "        xs[0] = 1;\n" +
                        "    }\n" +
                        "}\n").getBytes(StandardCharsets.UTF_8));
        Path out = Files.createTempDirectory("j2cpp-arr-out");
        Cmd.main(new String[]{"-src", src.toString(), "-out", out.toString()});
        Path generated = out.resolve("src/p/A.cpp");
        String cpp = new String(Files.readAllBytes(generated), StandardCharsets.UTF_8);
        assertTrue("make1 not emitted: " + cpp, cpp.contains("arrays::make1"));
        assertTrue("make2 not emitted: " + cpp, cpp.contains("arrays::make2"));
        compile(compiler, out, generated);
    }

    @Test
    public void listMapperSampleCompiles() throws Exception {
        Path compiler = compiler();
        Assume.assumeTrue("g++ is not available", compiler != null);

        Path out = convertSamples();
        Path generated = out.resolve("src/mapper/ListTest.cpp");
        String cpp = new String(Files.readAllBytes(generated), StandardCharsets.UTF_8);
        assertTrue("contains was not mapped: " + cpp, cpp.contains("std::find("));
        assertTrue("toArray was not mapped: " + cpp, cpp.contains("CppHelper::toArray("));
        compile(compiler, out, generated);
    }

    @Test
    public void renamedIdentifiersAndConstructorsCompile() throws Exception {
        Path compiler = compiler();
        Assume.assumeTrue("g++ is not available", compiler != null);

        Path out = convertSamples();
        String renamerHeader = new String(Files.readAllBytes(out.resolve("base/Renamer.h")), StandardCharsets.UTF_8);
        String fieldsHeader = new String(Files.readAllBytes(out.resolve("base/Fields.h")), StandardCharsets.UTF_8);
        assertTrue("C++ keyword method not renamed: " + renamerHeader, renamerHeader.contains("bool_renamed"));
        assertTrue("constructor given a return type: " + fieldsHeader, fieldsHeader.contains("Fields(int dummy);"));
        compile(compiler, out, out.resolve("src/base/Renamer.cpp"));
        compile(compiler, out, out.resolve("src/base/Fields.cpp"));
        compile(compiler, out, out.resolve("src/base/SuperCall.cpp"));
    }

    @Test
    public void nestedTypeHeadersCompile() throws Exception {
        Path compiler = compiler();
        Assume.assumeTrue("g++ is not available", compiler != null);

        Path out = convertSamples();
        String castHeader = new String(Files.readAllBytes(out.resolve("base/Cast.h")), StandardCharsets.UTF_8);
        String enumHeader = new String(Files.readAllBytes(out.resolve("base/Enum1.h")), StandardCharsets.UTF_8);
        assertTrue("inner class not included: " + castHeader, castHeader.contains("#include \"base/Cast_Derived.h\""));
        assertTrue("enum outside its namespace: " + enumHeader, enumHeader.contains("namespace base"));
        for (String name : new String[]{"Enum1", "SwitchTest", "iface", "Inner1", "Inner2Test"}) {
            compile(compiler, out, out.resolve("src/base/" + name + ".cpp"));
        }
    }

    @Test
    public void allSampleSourcesCompile() throws Exception {
        Path compiler = compiler();
        Assume.assumeTrue("g++ is not available", compiler != null);

        Path root = Path.of("src/test/resources/samples").toAbsolutePath();
        Path out = convertSamples();
        try (var sources = Files.walk(root)) {
            for (Path java : sources.filter(p -> p.toString().endsWith(".java")).sorted().toArray(Path[]::new)) {
                String rel = root.relativize(java).toString();
                Path cpp = out.resolve("src/" + rel.substring(0, rel.length() - 5) + ".cpp");
                assertTrue("no C++ output generated for " + rel, Files.exists(cpp));
            }
        }
        try (var files = Files.walk(out.resolve("src"))) {
            for (Path source : files.filter(p -> p.toString().endsWith(".cpp")).sorted().toArray(Path[]::new)) {
                compile(compiler, out, source);
            }
        }
    }

    @Test
    public void integerMapperSampleCompiles() throws Exception {
        Path compiler = compiler();
        Assume.assumeTrue("g++ is not available", compiler != null);

        Path out = convertSamples();
        Path generated = out.resolve("src/mapper/IntegerTest.cpp");
        String cpp = new String(Files.readAllBytes(generated), StandardCharsets.UTF_8);
        assertTrue("Integer methods were not mapped: " + cpp, cpp.contains("std::to_string(127)"));
        compile(compiler, out, generated);
    }
}
