package com.mesut;

import com.mesut.j2cpp.Cmd;
import org.junit.Assume;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/** The runtime helpers are split into domain headers; each must stand alone. */
public class RuntimeHeadersTest {

    private static final List<String> HEADERS = Arrays.asList(
            "j2cpp_str.h", "j2cpp_num.h", "j2cpp_obj.h", "j2cpp_rand.h", "j2cpp_arr.h",
            "j2cpp_fs.h", "j2cpp_io.h", "j2cpp_ex.h", "j2cpp_ctrl.h", "CppHelper.h");

    private static Path compiler() {
        return Arrays.stream(System.getenv("PATH").split(":"))
                .map(Path::of).map(d -> d.resolve("g++")).filter(Files::isExecutable)
                .findFirst().orElse(null);
    }

    private static void compile(Path compiler, Path source, String label) throws Exception {
        Process cp = new ProcessBuilder(compiler.toString(), "-std=c++17", "-fsyntax-only",
                "-I" + Path.of("cppRt").toAbsolutePath(), source.toString())
                .redirectErrorStream(true).start();
        String log = new String(cp.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals(label + " did not compile:\n" + log, 0,
                cp.waitFor(60, TimeUnit.SECONDS) ? cp.exitValue() : -1);
    }

    @Test
    public void eachHeaderCompilesStandalone() throws Exception {
        Path compiler = compiler();
        Assume.assumeTrue("g++ is not available", compiler != null);
        Path dir = Files.createTempDirectory("j2cpp-headers");
        for (String name : HEADERS) {
            Path src = dir.resolve(name + ".cpp");
            Files.write(src, ("#include \"" + name + "\"\nint main() { return 0; }\n")
                    .getBytes(StandardCharsets.UTF_8));
            compile(compiler, src, name);
        }
    }

    @Test
    public void generatedCodeUsesNarrowHeaders() throws Exception {
        Path src = Path.of("src/test/resources/samples").toAbsolutePath();
        Path out = Files.createTempDirectory("j2cpp-headers-out");
        Cmd.main(new String[]{"-src", src.toString(), "-out", out.toString()});
        try (Stream<Path> files = Files.walk(out.resolve("src"))) {
            for (Path cpp : files.filter(p -> p.toString().endsWith(".cpp")).toArray(Path[]::new)) {
                String text = new String(Files.readAllBytes(cpp), StandardCharsets.UTF_8);
                assertFalse("generated source still includes the umbrella: " + cpp,
                        text.contains("CppHelper.h"));
                if (text.contains("CppHelper::") || text.contains("arrays::")
                        || text.contains("j2cpp::")) {
                    assertTrue("no domain header included: " + cpp, text.contains("j2cpp_"));
                }
            }
        }
    }
}
