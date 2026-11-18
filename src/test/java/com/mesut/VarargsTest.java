package com.mesut;

import org.junit.Test;

import java.nio.file.Path;

/** Java varargs parameters and call sites. */
public class VarargsTest {

    private static final String JAVA = """
            package va;
            public class Va {
                static String join(String sep, String... parts) {
                    StringBuilder sb = new StringBuilder();
                    for (String p : parts) {
                        if (sb.length() > 0) sb.append(sep);
                        sb.append(p);
                    }
                    return sb.toString();
                }
                static int sum(int... xs) {
                    int r = 0;
                    for (int x : xs) r += x;
                    return r;
                }
                public static int main(String[] a) {
                    String s = join("-", "a", "b", "c");
                    String t = join("/", "z");
                    String[] arr = new String[]{"q", "r"};
                    String u = join(":", arr);
                    return (s.equals("a-b-c") ? 1 : 0) + (t.equals("z") ? 1 : 0)
                            + (u.equals("q:r") ? 1 : 0) + sum(1, 2, 3) + sum();
                }
            }
            """;

    private static final String MAIN = """
            #include "common.h"
            #include "va/Va.h"
            #include <cstdio>
            int main(){
                int r = va::Va::main(nullptr);
                printf("r=%d\\n", r);
                return r == 9 ? 0 : 1;
            }
            """;

    @Test
    public void varargsBecomeVectorParametersAndCalls() throws Exception {
        Path compiler = CppTestSupport.compiler();
        Path out = CppTestSupport.convert(JAVA, "va", "Va", "varargs");

        String header = CppTestSupport.generated(out, "va/Va.h");
        CppTestSupport.assertContains(header,
                "static std::string* join(std::string* sep, std::vector<std::string*>* parts)");
        CppTestSupport.assertContains(header, "static int sum(std::vector<int>* xs)");

        String cpp = CppTestSupport.generated(out, "src/va/Va.cpp");
        CppTestSupport.assertContains(cpp, "new std::vector<std::string*>{new std::string(\"a\")");
        CppTestSupport.assertContains(cpp, "Va::sum(new std::vector<int>{})");
        CppTestSupport.assertContains(cpp, "Va::join(new std::string(\":\"), arr)");

        CppTestSupport.compile(out, compiler, "src/va/Va.cpp", MAIN, "varargs_test")
                .assertOk("varargs test");
        CppTestSupport.runMerged(out.resolve("varargs_test")).assertOk("varargs test");
    }
}
