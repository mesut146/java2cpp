package com.mesut;

import com.mesut.j2cpp.Cmd;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/** Stage 1: method type variables erase to their bound, with notes in C++ and log.txt. */
public class GenericErasureTest {

    private static final String JAVA = """
            package g;
            public class G {
                public static <T> T identity(T x) { return x; }
                public static <T extends Comparable<T>> T max(T a, T b) {
                    return a.compareTo(b) > 0 ? a : b;
                }
                public static int main(String[] a) {
                    Integer v = identity(5);
                    return v;
                }
            }
            """;

    @Test
    public void erasureIsAnnotatedAndLogged() throws Exception {
        Path src = Files.createTempDirectory("j2cpp-erasure-src");
        Files.createDirectories(src.resolve("g"));
        Files.write(src.resolve("g/G.java"), JAVA.getBytes(StandardCharsets.UTF_8));
        Path out = Files.createTempDirectory("j2cpp-erasure-out");
        Cmd.main(new String[]{"-src", src.toString(), "-out", out.toString()});

        String header = new String(Files.readAllBytes(out.resolve("g/G.h")), StandardCharsets.UTF_8);
        assertTrue("method template missing in header:\n" + header,
                header.contains("template <class T>"));
        assertTrue("type variable not preserved:\n" + header,
                header.contains("static T identity(T x)") && header.contains("static T max(T a, T b)"));
        assertTrue("template note missing in header:\n" + header,
                header.contains("method type parameters kept as C++ template"));

        String cpp = new String(Files.readAllBytes(out.resolve("src/g/G.cpp")), StandardCharsets.UTF_8);
        assertTrue("template definition missing:\n" + cpp,
                cpp.contains("template <class T>\nT G::identity(T x)"));
        assertTrue("template note missing in source:\n" + cpp,
                cpp.contains("method type parameters kept as C++ template"));
        assertTrue("call-site note missing:\n" + cpp,
                cpp.contains("generic return type resolved by template deduction/erasure"));
        assertFalse("invalid dynamic_cast still emitted:\n" + cpp, cpp.contains("dynamic_cast<int>"));

        String log = new String(Files.readAllBytes(out.resolve("log.txt")), StandardCharsets.UTF_8);
        assertTrue("template not logged:\n" + log, log.contains("method type parameters kept as C++ template"));
        assertTrue("call-site rewrite not logged:\n" + log, log.contains("generic return type resolved"));
    }
}
