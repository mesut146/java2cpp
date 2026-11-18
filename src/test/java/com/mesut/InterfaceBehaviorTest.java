package com.mesut;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/** Java interfaces map to abstract C++ classes with virtual dispatch. */
public class InterfaceBehaviorTest {

    private static final String JAVA = """
            package i;
            interface Greeter {
                int BONUS = 10;
                String greet();
                default String label() { return "[" + greet() + "]"; }
                static Greeter make(String s) { return new Impl(s); }
            }
            class Impl implements Greeter {
                private String s;
                Impl(String s) { this.s = s; }
                public String greet() { return s; }
            }
            class Loud implements Greeter {
                public String greet() { return "x"; }
                public String label() { return "OVERRIDE"; }
            }
            public class I {
                public static int main(String[] a) {
                    Greeter g = Greeter.make("hi");
                    Greeter other = new Greeter() { public String greet() { return "anon"; } };
                    Greeter loud = new Loud();
                    Object o = g;
                    Greeter cast = (Greeter) o;
                    return cast.label().length() + other.label().length()
                            + loud.label().length() + Greeter.BONUS;
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
                return r == 28 ? 0 : 1;
            }
            """;

    @Test
    public void defaultStaticMethodsAndConstantsWork() throws Exception {
        Path compiler = CppTestSupport.compiler();

        Path out = CppTestSupport.convert(JAVA, "i", "I", "iface");

        String header = new String(Files.readAllBytes(out.resolve("i/Greeter.h")), StandardCharsets.UTF_8);
        assertTrue("abstract method should stay pure virtual:\n" + header,
                header.contains("virtual std::string* greet() = 0"));
        assertTrue("default method should be concrete and virtual:\n" + header,
                header.contains("virtual std::string* label()"));
        assertFalse("default method should not be pure virtual:\n" + header,
                header.contains("label() = 0"));
        assertTrue("static method should not be virtual:\n" + header,
                header.contains("static Greeter* make("));
        assertFalse("illegal static virtual emitted:\n" + header,
                header.contains("static virtual"));
        assertTrue("interface constant should be constexpr:\n" + header,
                header.contains("static constexpr int BONUS = 10"));

        CppTestSupport.compile(out, compiler, "src/i/I.cpp", MAIN, "iface_test")
                .assertOk("iface");
        CppTestSupport.runMerged(out.resolve("iface_test")).assertOk("iface");
    }
}
