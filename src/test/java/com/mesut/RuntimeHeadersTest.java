package com.mesut;

import com.mesut.j2cpp.Cmd;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Stream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/** The runtime helpers are split into domain headers; each must stand alone. */
public class RuntimeHeadersTest {

    private static final List<String> HEADERS = Arrays.asList(
            "j2cpp_str.h", "j2cpp_num.h", "j2cpp_obj.h", "j2cpp_rand.h", "j2cpp_arr.h",
            "j2cpp_fs.h", "j2cpp_io.h", "j2cpp_ex.h", "j2cpp_ctrl.h", "j2cpp_traits.h", "CppHelper.h");

    @Test
    public void eachHeaderCompilesStandalone() throws Exception {
        Path compiler = CppTestSupport.compiler();
        Path dir = Files.createTempDirectory("j2cpp-headers");
        ExecutorService pool = Executors.newFixedThreadPool(2);
        ConcurrentLinkedQueue<Throwable> errors = new ConcurrentLinkedQueue<>();
        List<Future<?>> futures = new ArrayList<>();
        for (String name : HEADERS) {
            Path src = dir.resolve(name + ".cpp");
            Files.write(src, ("#include \"" + name + "\"\nint main() { return 0; }\n")
                    .getBytes(StandardCharsets.UTF_8));
            futures.add(pool.submit(() -> {
                try {
                    CppTestSupport.RunResult result = CppTestSupport.compileOnly(dir, compiler, List.of(src));
                    org.junit.Assert.assertEquals(name + " did not compile:\n" + result.output, 0, result.exit);
                }
                catch (Throwable t) {
                    errors.add(t);
                }
            }));
        }
        for (Future<?> future : futures) future.get();
        pool.shutdown();
        assertTrue("parallel header compile failures: " + errors.toString(), errors.isEmpty());
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
