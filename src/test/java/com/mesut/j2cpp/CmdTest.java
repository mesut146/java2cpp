package com.mesut.j2cpp;

import com.mesut.j2cpp.Config;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertTrue;

public class CmdTest {

    private Path source(String name) throws Exception {
        Path src = Files.createTempDirectory("j2cpp-src");
        Files.createDirectories(src.resolve("demo"));
        Files.write(src.resolve("demo/" + name + ".java"),
                ("package demo;\n" +
                        "public class " + name + " {\n" +
                        "    public int add(int a, int b) { return a + b; }\n" +
                        "}\n").getBytes(StandardCharsets.UTF_8));
        return src;
    }

    @Test
    public void convertsSourceOnce() throws Exception {
        Path src = source("Simple");
        Path out = Files.createTempDirectory("j2cpp-out");

        Cmd.main(new String[]{"-src", src.toString(), "-out", out.toString()});

        assertTrue("header was not written", Files.exists(out.resolve("demo/Simple.h")));
        assertTrue("source was not written", Files.exists(out.resolve("src/demo/Simple.cpp")));
    }

    @Test
    public void missingPathsReportUsageWithoutThrowing() throws Exception {
        PrintStream oldErr = System.err;
        PrintStream oldOut = System.out;
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            System.setErr(new PrintStream(err, true, "UTF-8"));
            System.setOut(new PrintStream(out, true, "UTF-8"));
            Cmd.main(new String[]{"-out", "/tmp/j2cpp-unused"});
        }
        finally {
            System.setErr(oldErr);
            System.setOut(oldOut);
        }
        assertTrue(err.toString("UTF-8").contains("missing source path"));
        assertTrue(out.toString("UTF-8").contains("usage:"));
    }

    @Test
    public void noMappersKeepsJavaTypes() throws Exception {
        Path src = Files.createTempDirectory("j2cpp-src");
        Files.createDirectories(src.resolve("demo"));
        Files.write(src.resolve("demo/Raw.java"),
                ("package demo;\n" +
                        "import java.util.List;\n" +
                        "public class Raw {\n" +
                        "    public int test(List<Integer> xs) { xs.add(1); return xs.size(); }\n" +
                        "}\n").getBytes(StandardCharsets.UTF_8));
        Path out = Files.createTempDirectory("j2cpp-out");
        boolean old = Config.useMappers;
        try {
            Cmd.main(new String[]{"-no-mappers", "-src", src.toString(), "-out", out.toString()});
        }
        finally {
            Config.useMappers = old;
        }
        String cpp = new String(Files.readAllBytes(out.resolve("src/demo/Raw.cpp")), StandardCharsets.UTF_8);
        assertTrue("types were mapped: " + cpp, cpp.contains("java::util::List"));
        assertTrue("calls were mapped: " + cpp, cpp.contains("xs->add(1)"));
        assertTrue("unexpected mapper include: " + cpp, !cpp.contains("CppHelper.h"));
    }

    @Test
    public void mapsCppMethodsFromJson() throws Exception {
        Path src = Files.createTempDirectory("j2cpp-src");
        Files.createDirectories(src.resolve("demo"));
        Files.write(src.resolve("demo/MappedCalls.java"),
                ("package demo;\n" +
                        "import java.util.List;\n" +
                        "public class MappedCalls {\n" +
                        "    public String test(List<Integer> xs, String s) {\n" +
                        "        xs.add(1);\n" +
                        "        int n = xs.size();\n" +
                        "        String upper = s.toUpperCase();\n" +
                        "        return upper + n;\n" +
                        "    }\n" +
                        "}\n").getBytes(StandardCharsets.UTF_8));
        Path out = Files.createTempDirectory("j2cpp-out");
        Cmd.main(new String[]{"-src", src.toString(), "-out", out.toString()});
        String cpp = new String(Files.readAllBytes(out.resolve("src/demo/MappedCalls.cpp")), StandardCharsets.UTF_8);
        assertTrue("add() was not mapped: " + cpp, cpp.contains("xs->push_back(1)"));
        assertTrue("size() was not mapped: " + cpp, cpp.contains("xs->size()"));
        assertTrue("mapper include missing: " + cpp, cpp.contains("#include \"CppHelper.h\""));
        int preamble = cpp.indexOf("std::string *tmp");
        assertTrue("multi-line mapping missing: " + cpp, preamble >= 0);
        assertTrue("multi-line mapping emitted twice: " + cpp,
                cpp.indexOf("std::string *tmp", preamble + 1) < 0);
    }

    @Test
    public void mapperCanAppearBeforeSource() throws Exception {
        Path mapper = Files.createTempFile("j2cpp-mapper", ".json");
        Files.write(mapper, ("{\"name\":\"java.util.List<T>\",\"target\":\"std::vector\"," +
                "\"methods\":[]}").getBytes(StandardCharsets.UTF_8));
        Path src = source("Mapped");
        Path out = Files.createTempDirectory("j2cpp-out");

        Cmd.main(new String[]{"-mapper", mapper.toString(), "-src", src.toString(), "-out", out.toString()});

        assertTrue("source was not written", Files.exists(out.resolve("src/demo/Mapped.cpp")));
    }
}
