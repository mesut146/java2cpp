package com.mesut;

import com.mesut.j2cpp.Cmd;
import com.mesut.j2cpp.Config;
import org.junit.Assume;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/** The -no-mappers legacy path must still produce self-contained, compilable C++. */
public class NoMappersCompileTest {

    @Test
    public void allSamplesCompileWithoutMappers() throws Exception {
        Path compiler = Arrays.stream(System.getenv("PATH").split(":"))
                .map(Path::of).map(d -> d.resolve("g++")).filter(Files::isExecutable)
                .findFirst().orElse(null);
        Assume.assumeTrue("g++ is not available", compiler != null);

        Path src = Path.of("src/test/resources/samples").toAbsolutePath();
        Path out = Files.createTempDirectory("j2cpp-nomappers-out");
        boolean old = Config.useMappers;
        try {
            Cmd.main(new String[]{"-src", src.toString(), "-out", out.toString(), "-no-mappers"});
        }
        finally {
            Config.useMappers = old;
        }

        Path sourceDir = out.resolve("src");
        assertTrue("no generated sources", Files.isDirectory(sourceDir));
        try (Stream<Path> files = Files.walk(sourceDir)) {
            for (Path cpp : files.filter(p -> p.toString().endsWith(".cpp")).toArray(Path[]::new)) {
                Process process = new ProcessBuilder(compiler.toString(), "-std=c++17", "-fsyntax-only",
                        "-I" + Path.of("cppRt").toAbsolutePath(), "-I" + out, cpp.toString())
                        .redirectErrorStream(true).start();
                String log = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                assertEquals(cpp.getFileName() + " did not compile:\n" + log, 0,
                        process.waitFor(60, TimeUnit.SECONDS) ? process.exitValue() : -1);
            }
        }
    }
}
