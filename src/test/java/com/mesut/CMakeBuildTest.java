package com.mesut;

import com.mesut.j2cpp.Cmd;
import org.junit.Assume;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/** Verifies that the generated CMakeLists.txt lists all sources and builds. */
@Category(SlowTest.class)
public class CMakeBuildTest {

    @Test
    public void generatedCmakeProjectBuilds() throws Exception {
        Path compiler = CppTestSupport.compiler();
        Path cmake = Arrays.stream(System.getenv("PATH").split(":"))
                .map(Path::of).map(d -> d.resolve("cmake")).filter(Files::isExecutable)
                .findFirst().orElse(null);
        Assume.assumeTrue("cmake not available", cmake != null);

        Path src = Path.of("src/test/resources/samples").toAbsolutePath();
        Path out = Files.createTempDirectory("j2cpp-cmake-out");
        Cmd.main(new String[]{"-src", src.toString(), "-out", out.toString()});

        String cmakeLists = new String(Files.readAllBytes(out.resolve("CMakeLists.txt")), StandardCharsets.UTF_8);
        assertTrue("no sources in CMakeLists:\n" + cmakeLists, cmakeLists.contains("src/base/Box.cpp"));
        assertTrue("library target missing:\n" + cmakeLists, cmakeLists.contains("add_library(mylib STATIC"));
        assertTrue("runtime include dir missing:\n" + cmakeLists, cmakeLists.contains("cppRt"));
        assertTrue("runtime headers not copied", Files.isRegularFile(out.resolve("cppRt/j2cpp_str.h")));

        Path build = Files.createDirectories(out.resolve("build"));
        Process cfg = new ProcessBuilder(cmake.toString(), "..",
                "-DCMAKE_CXX_COMPILER=" + compiler)
                .directory(build.toFile()).redirectErrorStream(true).start();
        String cfgLog = new String(cfg.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals("cmake configure failed:\n" + cfgLog, 0,
                cfg.waitFor(120, TimeUnit.SECONDS) ? cfg.exitValue() : -1);

        ProcessBuilder buildBuilder = new ProcessBuilder(cmake.toString(), "--build", ".", "-j2")
                .directory(build.toFile()).redirectErrorStream(true);
        buildBuilder.environment().put("CCACHE_BASEDIR", out.toString());
        Process b = buildBuilder.start();
        String bLog = new String(b.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals("cmake build failed:\n" + bLog, 0,
                b.waitFor(180, TimeUnit.SECONDS) ? b.exitValue() : -1);
        assertTrue("library not produced", Files.exists(build.resolve("libmylib.a"))
                || Files.exists(build.resolve("libmylib.so")));
    }
}
