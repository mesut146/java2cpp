package com.mesut;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/** Mutually referencing source classes must not include each other's headers. */
public class ForwardDeclarationTest {

    private static final String A = """
            package fd;
            public class A { B b; }
            """;

    private static final String B = """
            package fd;
            class B { A a; }
            """;

    @Test
    public void pointerFieldsUseForwardDeclarations() throws Exception {
        Path src = Files.createTempDirectory("j2cpp-fwd-src");
        Files.createDirectories(src.resolve("fd"));
        Files.write(src.resolve("fd/A.java"), A.getBytes(StandardCharsets.UTF_8));
        Files.write(src.resolve("fd/B.java"), B.getBytes(StandardCharsets.UTF_8));
        Path out = Files.createTempDirectory("j2cpp-fwd-out");
        com.mesut.j2cpp.Cmd.main(new String[]{"-src", src.toString(), "-out", out.toString()});

        String headerA = CppTestSupport.generated(out, "fd/A.h");
        assertTrue("B not forward declared:\n" + headerA, headerA.contains("class B;"));
        assertFalse("circular include emitted:\n" + headerA, headerA.contains("#include \"fd/B.h\""));

        String headerB = CppTestSupport.generated(out, "fd/B.h");
        assertTrue("A not forward declared:\n" + headerB, headerB.contains("class A;"));
        assertFalse("circular include emitted:\n" + headerB, headerB.contains("#include \"fd/A.h\""));
    }

    @Test
    public void crossPackageForwardsUseTheirOwnNamespace() throws Exception {
        Path src = Files.createTempDirectory("j2cpp-fwd-ns-src");
        Files.createDirectories(src.resolve("fa"));
        Files.createDirectories(src.resolve("fb"));
        Files.write(src.resolve("fa/A.java"),
                "package fa; public class A { fb.B b; }".getBytes(StandardCharsets.UTF_8));
        Files.write(src.resolve("fb/B.java"),
                "package fb; public class B {}".getBytes(StandardCharsets.UTF_8));
        Path out = Files.createTempDirectory("j2cpp-fwd-ns-out");
        com.mesut.j2cpp.Cmd.main(new String[]{"-src", src.toString(), "-out", out.toString()});

        String header = CppTestSupport.generated(out, "fa/A.h");
        assertTrue("cross-package forward declaration missing:\n" + header,
                header.contains("namespace fb {") && header.contains("class B;"));
        assertFalse("wrong namespace include:\n" + header, header.contains("#include \"fb/B.h\""));
    }
}
