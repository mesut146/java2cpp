package com.mesut;

import org.junit.Test;

import java.nio.file.Path;

import static org.junit.Assert.assertTrue;

/** Generated boxed-primitive stubs must be usable and linkable without mappers. */
public class StubWrapperTest {

    private static final String JAVA = """
            package i;
            public class I {
                public static int main(String[] a) {
                    Integer i = 7;
                    int x = i.intValue();
                    Integer j = Integer.valueOf(5);
                    return x + j.intValue() + i.hashCode();
                }
            }
            """;

    private static final String MAIN = """
            #include "common.h"
            #include "i/I.h"
            #include <cstdio>
            int main(){
                int r = i::I::main(nullptr);
                printf("r=%d\\n", r);
                return r == 19 ? 0 : 1;
            }
            """;

    @Test
    public void integerStubStoresAndReturnsItsValue() throws Exception {
        Path compiler = CppTestSupport.compiler();
        Path out = CppTestSupport.convertNoMappers(JAVA, "i", "I", "stub-wrapper");

        String header = CppTestSupport.generated(out, "lib/java/lang/Integer.h");
        assertTrue("constructor does not store the value:\n" + header,
                header.contains("Integer(int v) : value(v) {}"));
        assertTrue("unboxing method missing:\n" + header,
                header.contains("int intValue() { return value; }"));
        assertTrue("valueOf does not use the constructor:\n" + header,
                header.contains("static Integer* valueOf(int p0) { return new Integer(p0); }"));

        CppTestSupport.compile(out, compiler, "src/i/I.cpp", MAIN, "stub_wrapper_test")
                .assertOk("stub wrapper test");
        CppTestSupport.runMerged(out.resolve("stub_wrapper_test")).assertOk("stub wrapper test");
    }
}
