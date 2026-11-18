package com.mesut;

import com.mesut.j2cpp.Cmd;
import com.mesut.j2cpp.Config;
import org.junit.Assume;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

/** Shared harness for the compile-and-run tests: convert Java, compile with g++, run. */
final class CppTestSupport {

    private CppTestSupport() {
    }

    public static final class RunResult {
        public final int exit;
        public final String output;
        public final String stdout;
        public final String stderr;

        private RunResult(int exit, String output, String stdout, String stderr) {
            this.exit = exit;
            this.output = output;
            this.stdout = stdout;
            this.stderr = stderr;
        }

        public void assertOk(String label) {
            assertEquals(label + "\n" + output, 0, exit);
        }
    }

    /** Find g++ or skip the test. */
    public static Path compiler() {
        Path compiler = Arrays.stream(System.getenv("PATH").split(":"))
                .map(Path::of).map(d -> d.resolve("g++")).filter(Files::isExecutable)
                .findFirst().orElse(null);
        Assume.assumeTrue("g++ is not available", compiler != null);
        return compiler;
    }

    /** Write a single Java source into a temp tree and convert it with mappers. */
    public static Path convert(String java, String pkg, String cls, String prefix) throws IOException {
        return convert(java, pkg, cls, prefix, true);
    }

    /** Convert with -no-mappers to exercise the generated stub headers. */
    public static Path convertNoMappers(String java, String pkg, String cls, String prefix)
            throws IOException {
        return convert(java, pkg, cls, prefix, false);
    }

    /** Convert a source that depends on a compiled classpath jar or directory. */
    public static Path convertWithClasspath(String java, String pkg, String cls, String prefix,
                                            Path classpath) throws IOException {
        Path src = Files.createTempDirectory("j2cpp-" + prefix + "-src");
        Path dir = src.resolve(pkg);
        Files.createDirectories(dir);
        Files.write(dir.resolve(cls + ".java"), java.getBytes(StandardCharsets.UTF_8));
        Path out = Files.createTempDirectory("j2cpp-" + prefix + "-out");
        Cmd.main(new String[]{"-src", src.toString(), "-out", out.toString(),
                "-cp", classpath.toString()});
        return out;
    }

    private static Path convert(String java, String pkg, String cls, String prefix,
                                boolean useMappers) throws IOException {
        Path src = Files.createTempDirectory("j2cpp-" + prefix + "-src");
        Path dir = src.resolve(pkg);
        Files.createDirectories(dir);
        Files.write(dir.resolve(cls + ".java"), java.getBytes(StandardCharsets.UTF_8));
        Path out = Files.createTempDirectory("j2cpp-" + prefix + "-out");
        String[] args = useMappers
                ? new String[]{"-src", src.toString(), "-out", out.toString()}
                : new String[]{"-src", src.toString(), "-out", out.toString(), "-no-mappers"};
        boolean old = Config.useMappers;
        try {
            Config.useMappers = useMappers;
            Cmd.main(args);
        }
        finally {
            Config.useMappers = old;
        }
        return out;
    }

    /** Read a generated source or header relative to the output directory. */
    public static String generated(Path out, String relativePath) throws IOException {
        return new String(Files.readAllBytes(out.resolve(relativePath)), StandardCharsets.UTF_8);
    }

    /** Read the conversion log. */
    public static String log(Path out) throws IOException {
        return generated(out, "log.txt");
    }

    /** Compile generated sources plus a small main() and link an executable. */
    public static RunResult compile(Path out, Path compiler, List<Path> sources,
                                    String main, String exeName) throws Exception {
        Path mainFile = null;
        List<String> command = new ArrayList<>();
        command.add(compiler.toString());
        command.add("-std=c++17");
        command.add("-I" + Path.of("cppRt").toAbsolutePath());
        command.add("-I" + out.toString());
        if (main != null) {
            mainFile = out.resolve("main.cpp");
            Files.write(mainFile, main.getBytes(StandardCharsets.UTF_8));
            command.add(mainFile.toString());
        }
        for (Path source : sources) {
            command.add(source.toString());
        }
        Path exe = out.resolve(exeName);
        command.add("-o");
        command.add(exe.toString());
        Process cp = new ProcessBuilder(command).redirectErrorStream(true).start();
        String log = new String(cp.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        int exit = cp.waitFor(60, TimeUnit.SECONDS) ? cp.exitValue() : -1;
        assertEquals("compile/link failed:\n" + log, 0, exit);
        return new RunResult(exit, log, log, "");
    }

    /** Compile a single generated source and link it with main(). */
    public static RunResult compile(Path out, Path compiler, String sourceRelative,
                                    String main, String exeName) throws Exception {
        return compile(out, compiler, Arrays.asList(out.resolve(sourceRelative)), main, exeName);
    }

    /** Syntax-check generated sources without linking. */
    public static RunResult compileOnly(Path out, Path compiler, List<Path> sources,
                                        String... extraFlags) throws Exception {
        List<String> command = new ArrayList<>();
        command.add(compiler.toString());
        command.add("-std=c++17");
        command.add("-fsyntax-only");
        command.add("-I" + Path.of("cppRt").toAbsolutePath());
        command.add("-I" + out.toString());
        command.addAll(Arrays.asList(extraFlags));
        for (Path source : sources) {
            command.add(source.toString());
        }
        Process cp = new ProcessBuilder(command).redirectErrorStream(true).start();
        String log = new String(cp.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        return new RunResult(cp.waitFor(60, TimeUnit.SECONDS) ? cp.exitValue() : -1, log, log, "");
    }

    /** Run an executable, merging stdout and stderr. */
    public static RunResult runMerged(Path exe) throws Exception {
        Process run = new ProcessBuilder(exe.toString()).redirectErrorStream(true).start();
        String log = new String(run.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        return new RunResult(run.waitFor(60, TimeUnit.SECONDS) ? run.exitValue() : -1, log, log, "");
    }

    /** Run an executable, keeping stdout and stderr separate. */
    public static RunResult runSeparate(Path exe) throws Exception {
        Process run = new ProcessBuilder(exe.toString()).start();
        String stdout = new String(run.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        String stderr = new String(run.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
        int exit = run.waitFor(60, TimeUnit.SECONDS) ? run.exitValue() : -1;
        return new RunResult(exit, stdout + stderr, stdout, stderr);
    }

    public static void assertContains(String text, String fragment) {
        org.junit.Assert.assertTrue("missing \"" + fragment + "\" in:\n" + text,
                text.contains(fragment));
    }
}
