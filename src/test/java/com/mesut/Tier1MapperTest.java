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

/** Tier 1 stdlib: missing methods on List/Map/Set/Arrays/String map to native C++. */
public class Tier1MapperTest {

    private static final String JAVA = """
            package t1;
            import java.util.*;
            public class T1 {
                public static int main(String[] a) {
                    int r = 0;
                    List<Integer> l = new ArrayList<Integer>();
                    l.add(3); l.add(1); l.add(2);
                    l.sort(null);
                    List<Integer> sub = l.subList(1, 3);

                    Map<Integer,Integer> m = new HashMap<Integer,Integer>();
                    m.put(1, 10); m.put(2, 20);
                    if (m.containsKey(1)) r += 1;
                    if (m.containsValue(20)) r += 2;
                    r += m.getOrDefault(9, 7);

                    Set<Integer> s = new HashSet<Integer>();
                    s.add(1); s.add(2); s.add(3);
                    List<Integer> keep = new ArrayList<Integer>(); keep.add(2); keep.add(3);
                    s.retainAll(keep);
                    List<Integer> drop = new ArrayList<Integer>(); drop.add(3);
                    s.removeAll(drop);
                    r += s.size() * 10;

                    int[] arr = {5,4,3};
                    Arrays.sort(arr);
                    int[] cp = Arrays.copyOf(arr, 3);
                    Arrays.fill(cp, 7);
                    if (Arrays.equals(cp, cp)) r += 100;

                    String rep = "ab".repeat(2);
                    String st = "  hi  ".strip();
                    boolean blank = "  ".isBlank();
                    int ci = "AbC".compareToIgnoreCase("abc");
                    List<String> ls = "a\\nb\\nc".lines();
                    String j = String.join("-", ls);
                    String jv = String.join(",", "x", "y");
                    if (rep.equals("abab")) r += 1000;
                    if (st.equals("hi")) r += 2000;
                    if (!blank) r += 4000;
                    if (ci == 0) r += 8000;
                    if (j.equals("a-b-c")) r += 16000;
                    if (jv.equals("x,y")) r += 32000;
                    return r;
                }
            }
            """;

    private static final String MAIN = """
            #include "common.h"
            #include "t1/T1.h"
            #include <cstdio>
            int main(){
                int r = t1::T1::main(nullptr);
                printf("r=%d\\n", r);
                return r == 59120 ? 0 : 1;
            }
            """;

    @Test
    public void tier1Mappings() throws Exception {
        Path compiler = Arrays.stream(System.getenv("PATH").split(":"))
                .map(Path::of).map(d -> d.resolve("g++")).filter(Files::isExecutable)
                .findFirst().orElse(null);
        Assume.assumeTrue("g++ is not available", compiler != null);

        Path src = Files.createTempDirectory("j2cpp-t1-src");
        Files.createDirectories(src.resolve("t1"));
        Files.write(src.resolve("t1/T1.java"), JAVA.getBytes(StandardCharsets.UTF_8));
        Path out = Files.createTempDirectory("j2cpp-t1-out");
        Cmd.main(new String[]{"-src", src.toString(), "-out", out.toString()});

        String gen = new String(Files.readAllBytes(out.resolve("src/t1/T1.cpp")), StandardCharsets.UTF_8);
        assertTrue("List.sort not mapped:\n" + gen, gen.contains("std::sort(l->begin()"));
        assertTrue("Map.containsKey not mapped:\n" + gen, gen.contains("m->find(1)"));
        assertTrue("Set.retainAll not mapped:\n" + gen, gen.contains("std::find(keep->begin()"));
        assertTrue("Arrays.sort not mapped:\n" + gen, gen.contains("std::sort(arr->begin()"));
        assertTrue("String.repeat not mapped:\n" + gen, gen.contains("CppHelper::repeat("));
        assertTrue("String.join not mapped:\n" + gen, gen.contains("CppHelper::join("));
        assertTrue("array initializer not wrapped:\n" + gen, gen.contains("arrays::from({5, 4, 3})"));

        Path main = out.resolve("main.cpp");
        Files.write(main, MAIN.getBytes(StandardCharsets.UTF_8));
        Path exe = out.resolve("t1_test");
        Process cp = new ProcessBuilder(compiler.toString(), "-std=c++17",
                "-I" + Path.of("cppRt").toAbsolutePath(), "-I" + out,
                out.resolve("src/t1/T1.cpp").toString(), main.toString(), "-o", exe.toString())
                .redirectErrorStream(true).start();
        String compileLog = new String(cp.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals("compile failed:\n" + compileLog, 0,
                cp.waitFor(60, TimeUnit.SECONDS) ? cp.exitValue() : -1);

        Process run = new ProcessBuilder(exe.toString()).redirectErrorStream(true).start();
        String log = new String(run.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals("run output:\n" + log, 0, run.waitFor(60, TimeUnit.SECONDS) ? run.exitValue() : -1);
    }
}
