package com.mesut;

import org.junit.Test;

import javax.tools.ToolProvider;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;

/** Raw JDK generic types (Class, Stream) need Object arguments in generated stubs. */
public class RawClassGenericTest {

    private static final String LIB = """
            package lib;
            public class ClassLegacy {
                public static Class rawClass(Class c) { return c; }
                public static java.util.stream.Stream rawStream(java.util.stream.Stream s) { return s; }
            }
            """;

    private static final String JAVA = """
            package use;
            public class U {
                public static int main(String[] a) {
                    return (lib.ClassLegacy.rawClass(null) == null
                            && lib.ClassLegacy.rawStream(null) == null) ? 1 : 0;
                }
            }
            """;

    private static final String MAIN = """
            #include "common.h"
            #include "use/U.h"
            #include <cstdio>
            int main(){
                int r = use::U::main(nullptr);
                printf("r=%d\\n", r);
                return r == 1 ? 0 : 1;
            }
            """;

    @Test
    public void rawClassAndStreamAreTemplated() throws Exception {
        Path libSrc = Files.createTempDirectory("j2cpp-gc-libsrc");
        Files.createDirectories(libSrc.resolve("lib"));
        Path libFile = libSrc.resolve("lib/ClassLegacy.java");
        Files.write(libFile, LIB.getBytes(StandardCharsets.UTF_8));
        Path classes = Files.createTempDirectory("j2cpp-gc-classes");
        assertEquals("javac failed", 0, ToolProvider.getSystemJavaCompiler()
                .run(null, null, null, "-d", classes.toString(), libFile.toString()));
        Path jar = Files.createTempDirectory("j2cpp-gc-jar").resolve("legacy.jar");
        assertEquals("jar failed", 0, new ProcessBuilder("jar", "cf", jar.toString(),
                "-C", classes.toString(), ".").redirectErrorStream(true).start().waitFor());

        Path compiler = CppTestSupport.compiler();
        Path out = CppTestSupport.convertWithClasspath(JAVA, "use", "U", "raw-class", jar);

        String stub = CppTestSupport.generated(out, "lib/lib/ClassLegacy.h");
        CppTestSupport.assertContains(stub, "Class<java::lang::Object*>* rawClass(");
        CppTestSupport.assertContains(stub, "Stream<java::lang::Object*>* rawStream(");

        CppTestSupport.compile(out, compiler, "src/use/U.cpp", MAIN, "raw_class_test")
                .assertOk("raw class generic test");
        CppTestSupport.runMerged(out.resolve("raw_class_test")).assertOk("raw class generic test");
    }
}
