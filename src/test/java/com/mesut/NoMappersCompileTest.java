package com.mesut;

import com.mesut.j2cpp.Cmd;
import com.mesut.j2cpp.Config;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.Assert.assertTrue;

/** The -no-mappers legacy path must still produce self-contained, compilable C++. */
public class NoMappersCompileTest {

    @Test
    public void allSamplesCompileWithoutMappers() throws Exception {
        Path compiler = CppTestSupport.compiler();

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
                CppTestSupport.RunResult result = CppTestSupport.compileOnly(out, compiler, List.of(cpp));
                org.junit.Assert.assertEquals(cpp.getFileName() + " did not compile:\n" + result.output,
                        0, result.exit);
            }
        }
    }
}
