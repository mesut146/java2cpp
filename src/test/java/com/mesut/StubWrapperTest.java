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
                    Boolean b = true;
                    int x = i.intValue();
                    Integer j = Integer.valueOf(5);
                    return x + j.intValue() + i.hashCode() + (b.booleanValue() ? 1 : 0);
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
                return r == 20 ? 0 : 1;
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

        String boolHeader = CppTestSupport.generated(out, "lib/java/lang/Boolean.h");
        assertTrue("Boolean constructor does not store the value:\n" + boolHeader,
                boolHeader.contains("Boolean(bool v) : value(v) {}"));
        assertTrue("Boolean unboxing method missing:\n" + boolHeader,
                boolHeader.contains("bool booleanValue() { return value; }"));

        String forwards = CppTestSupport.generated(out, "lib/lib_common.h");
        assertTrue("nested generic type not forward declared:\n" + forwards,
                forwards.contains("template <typename T>") && forwards.contains("class DynamicConstantDesc;"));

        CppTestSupport.compile(out, compiler, "src/i/I.cpp", MAIN, "stub_wrapper_test")
                .assertOk("stub wrapper test");
        CppTestSupport.runMerged(out.resolve("stub_wrapper_test")).assertOk("stub wrapper test");
    }
}
