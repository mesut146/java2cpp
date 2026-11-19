package com.mesut;

import com.mesut.j2cpp.Cmd;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Source classes referenced only in method bodies need their headers included. */
public class SourceBodyIncludeTest {

    private static final String HELPER = """
            package hp;
            public class Helper { public static int VALUE = 7; }
            """;

    private static final String USE = """
            package si;
            import hp.Helper;
            public class Use {
                public static int main(String[] a) { return Helper.VALUE; }
            }
            """;

    private static final String MAIN = """
            #include "common.h"
            #include "si/Use.h"
            #include <cstdio>
            int main(){
                int r = si::Use::main(nullptr);
                printf("r=%d\\n", r);
                return r == 7 ? 0 : 1;
            }
            """;

    @Test
    public void methodBodySourceTypesAreIncluded() throws Exception {
        Path src = Files.createTempDirectory("j2cpp-si-src");
        Files.createDirectories(src.resolve("hp"));
        Files.createDirectories(src.resolve("si"));
        Files.write(src.resolve("hp/Helper.java"), HELPER.getBytes(StandardCharsets.UTF_8));
        Files.write(src.resolve("si/Use.java"), USE.getBytes(StandardCharsets.UTF_8));
        Path out = Files.createTempDirectory("j2cpp-si-out");
        Cmd.main(new String[]{"-src", src.toString(), "-out", out.toString()});

        String cpp = CppTestSupport.generated(out, "src/si/Use.cpp");
        CppTestSupport.assertContains(cpp, "#include \"hp/Helper.h\"");

        Path compiler = CppTestSupport.compiler();
        CppTestSupport.compile(out, compiler,
                List.of(out.resolve("src/si/Use.cpp"), out.resolve("src/hp/Helper.cpp")),
                MAIN, "si_test")
                .assertOk("source body include test");
        CppTestSupport.runMerged(out.resolve("si_test")).assertOk("source body include test");
    }
}
