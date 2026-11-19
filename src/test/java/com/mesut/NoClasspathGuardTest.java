package com.mesut;

import com.mesut.j2cpp.Cmd;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertTrue;

/** Unresolved enhanced-for types must be noted, not crash the converter. */
public class NoClasspathGuardTest {

    private static final String JAVA = """
            package np;
            public class N {
                void f() {
                    for (Object v : Missing.map.values()) {
                        int y = 1;
                    }
                }
            }
            """;

    @Test
    public void unresolvedEnhancedForIsOmittedWithNote() throws Exception {
        Path src = Files.createTempDirectory("j2cpp-nocp-src");
        Files.createDirectories(src.resolve("np"));
        Files.write(src.resolve("np/N.java"), JAVA.getBytes(StandardCharsets.UTF_8));
        Path out = Files.createTempDirectory("j2cpp-nocp-out");

        Cmd.main(new String[]{"-src", src.toString(), "-out", out.toString()});

        String cpp = CppTestSupport.generated(out, "src/np/N.cpp");
        assertTrue("unresolved loop not annotated:\n" + cpp,
                cpp.contains("enhanced-for type unresolved; loop omitted"));
        String log = CppTestSupport.log(out);
        assertTrue("unresolved loop not logged:\n" + log, log.contains("enhanced-for type is unresolved"));
    }

    @Test
    public void unresolvedCastDoesNotCrash() throws Exception {
        String source = """
                package np;
                public class C {
                    void f(Object o) {
                        Object x = (Missing) o;
                    }
                }
                """;
        Path src = Files.createTempDirectory("j2cpp-nocp-cast-src");
        Files.createDirectories(src.resolve("np"));
        Files.write(src.resolve("np/C.java"), source.getBytes(StandardCharsets.UTF_8));
        Path out = Files.createTempDirectory("j2cpp-nocp-cast-out");

        Cmd.main(new String[]{"-src", src.toString(), "-out", out.toString()});

        String cpp = CppTestSupport.generated(out, "src/np/C.cpp");
        assertTrue("unresolved cast not emitted:\n" + cpp, cpp.contains("dynamic_cast"));
    }
}
