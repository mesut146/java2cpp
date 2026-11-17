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

/** Verifies that the generated CMakeLists.txt lists all sources and builds. */
public class CMakeBuildTest {

    @Test
    public void generatedCmakeProjectBuilds() throws Exception {
        Path compiler = Arrays.stream(System.getenv("PATH").split(":"))
                .map(Path::of).map(d -> d.resolve("g++")).filter(Files::isExecutable)
                .findFirst().orElse(null);
        Path cmake = Arrays.stream(System.getenv("PATH").split(":"))
                .map(Path::of).map(d -> d.resolve("cmake")).filter(Files::isExecutable)
                .findFirst().orElse(null);
        Assume.assumeTrue("g++/cmake not available", compiler != null && cmake != null);

        Path src = Path.of("src/test/resources/samples").toAbsolutePath();
        Path out = Files.createTempDirectory("j2cpp-cmake-out");
        Cmd.main(new String[]{"-src", src.toString(), "-out", out.toString()});

        String cmakeLists = new String(Files.readAllBytes(out.resolve("CMakeLists.txt")), StandardCharsets.UTF_8);
        assertTrue("no sources in CMakeLists:\n" + cmakeLists, cmakeLists.contains("src/base/Box.cpp"));
        assertTrue("library target missing:\n" + cmakeLists, cmakeLists.contains("add_library(mylib STATIC"));

        Path build = Files.createDirectories(out.resolve("build"));
        Process cfg = new ProcessBuilder(cmake.toString(), "..",
                "-DCMAKE_CXX_COMPILER=" + compiler,
                "-DCMAKE_CXX_FLAGS=-I" + Path.of("cppRt").toAbsolutePath())
                .directory(build.toFile()).redirectErrorStream(true).start();
        String cfgLog = new String(cfg.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals("cmake configure failed:\n" + cfgLog, 0,
                cfg.waitFor(120, TimeUnit.SECONDS) ? cfg.exitValue() : -1);

        Process b = new ProcessBuilder(cmake.toString(), "--build", ".")
                .directory(build.toFile()).redirectErrorStream(true).start();
        String bLog = new String(b.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals("cmake build failed:\n" + bLog, 0,
                b.waitFor(180, TimeUnit.SECONDS) ? b.exitValue() : -1);
        assertTrue("library not produced", Files.exists(build.resolve("libmylib.a"))
                || Files.exists(build.resolve("libmylib.so")));
    }
}
