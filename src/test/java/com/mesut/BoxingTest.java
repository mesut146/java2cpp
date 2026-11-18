package com.mesut;

import org.junit.Test;

import java.nio.file.Path;


/** Implicit Java boxing/unboxing in Object-typed positions. */
public class BoxingTest {

    private static final String JAVA = """
            package b;
            public class B {
                public static int main(String[] a) {
                    Object o = 5;
                    Integer i = 7;
                    Object fromVar = i;
                    Number n = i;
                    Object t = true;
                    int x = i;
                    return (Integer) o + x + (Integer) fromVar + n.intValue()
                            + (((Boolean) t) ? 1 : 0);
                }
            }
            """;

    private static final String MAIN = """
            #include "common.h"
            #include "b/B.h"
            #include <cstdio>
            int main(){
                int r = b::B::main(nullptr);
                printf("r=%d\\n", r);
                return r == 27 ? 0 : 1;
            }
            """;

    private static final String BOX_JAVA =
            "package b;\n" +
            "import java.util.List;\n" +
            "import java.util.ArrayList;\n" +
            "public class Box {\n" +
            "    public int test() {\n" +
            "        Integer boxed = Integer.valueOf(7);\n" +
            "        int unboxed = boxed;\n" +
            "        List<Integer> xs = new ArrayList<>();\n" +
            "        xs.add(3);\n" +
            "        xs.add(5);\n" +
            "        int sum = xs.get(0) + xs.get(1);\n" +
            "        int[] arr = new int[3];\n" +
            "        arr[0] = 10; arr[1] = 20; arr[2] = 30;\n" +
            "        return unboxed * 1000 + sum * 100 + arr[1] + arr.length;\n" +
            "    }\n" +
            "}\n";

    private static final String BOX_MAIN =
            "#include \"common.h\"\n" +
            "#include \"b/Box.h\"\n" +
            "#include <cstdio>\n" +
            "int main(){ b::Box o; int r=o.test(); printf(\"r=%d\\n\", r); return r==7823?0:1; }\n";

    @Test
    public void primitivesBoxToObjectAndCastBack() throws Exception {
        Path compiler = CppTestSupport.compiler();
        Path out = CppTestSupport.convert(JAVA, "b", "B", "box");

        String cpp = CppTestSupport.generated(out, "src/b/B.cpp");
        CppTestSupport.assertContains(cpp, "new ::j2cpp::Box<int>(5)");
        CppTestSupport.assertContains(cpp, "new ::j2cpp::Box<int>(i)");
        CppTestSupport.assertContains(cpp, "new ::j2cpp::Box<bool>(true)");
        CppTestSupport.assertContains(cpp, "j2cpp::unbox<int>(o)");
        CppTestSupport.assertContains(cpp, "new CppHelper::Number(i)");

        String log = CppTestSupport.log(out);
        CppTestSupport.assertContains(log, "primitive boxed for Object-typed target");
        CppTestSupport.assertContains(log, "cast to boxed primitive unboxed");

        CppTestSupport.compile(out, compiler, "src/b/B.cpp", MAIN, "box_test")
                .assertOk("boxing test");
        CppTestSupport.runMerged(out.resolve("box_test")).assertOk("boxing test");
    }

    @Test
    public void boxedIntegersListsAndArraysBehave() throws Exception {
        Path compiler = CppTestSupport.compiler();
        Path out = CppTestSupport.convert(BOX_JAVA, "b", "Box", "box-runtime");

        CppTestSupport.compile(out, compiler, "src/b/Box.cpp", BOX_MAIN, "box2_test")
                .assertOk("boxing/array test");
        CppTestSupport.runMerged(out.resolve("box2_test")).assertOk("boxing/array test");
    }
}
