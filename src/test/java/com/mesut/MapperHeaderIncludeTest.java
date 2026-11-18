package com.mesut;

import org.junit.Test;

import java.nio.file.Path;

/** Generated headers must include the runtime headers for mapper types they use. */
public class MapperHeaderIncludeTest {

    private static final String JAVA = """
            package mi;
            public class M {
                StringBuilder sb;
                StringBuilder make() { return new StringBuilder(); }
            }
            """;

    //deliberately includes the generated header before any mapper header
    private static final String MAIN = """
            #include "common.h"
            #include "mi/M.h"
            #include <cstdio>
            int main(){
                mi::M m;
                m.sb = m.make();
                m.sb->append("ok");
                printf("%s\\n", m.sb->toString()->c_str());
                return 0;
            }
            """;

    @Test
    public void headerCarriesMapperIncludes() throws Exception {
        Path compiler = CppTestSupport.compiler();
        Path out = CppTestSupport.convert(JAVA, "mi", "M", "mapper-include");

        String header = CppTestSupport.generated(out, "mi/M.h");
        CppTestSupport.assertContains(header, "#include \"j2cpp_str.h\"");

        CppTestSupport.compile(out, compiler, "src/mi/M.cpp", MAIN, "mapper_include_test")
                .assertOk("mapper header include test");
        CppTestSupport.runMerged(out.resolve("mapper_include_test")).assertOk("mapper header include test");
    }
}
