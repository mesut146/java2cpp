package com.mesut;

import org.junit.Test;

import java.nio.file.Path;


/** Verifies that the String mapper methods behave like their Java counterparts. */
public class StringMapperBehaviorTest {

    private static final String JAVA =
            "package s;\n" +
            "public class Str {\n" +
            "    public String upper(String x) { return x.toUpperCase(); }\n" +
            "    public boolean contains(String x, String y) { return x.contains(y); }\n" +
            "    public int index(String x, String y) { return x.indexOf(y); }\n" +
            "    public String replace(String x, String a, String b) { return x.replace(a, b); }\n" +
            "}\n";

    private static final String MAIN =
            "#include \"common.h\"\n" +
            "#include \"s/Str.h\"\n" +
            "#include <cstdio>\n" +
            "#include <string>\n" +
            "int main(){\n" +
            "    s::Str o;\n" +
            "    std::string x = \"Hello World\";\n" +
            "    std::string y = \"World\";\n" +
            "    std::string* r = o.upper(&x);\n" +
            "    bool c = o.contains(&x, &y);\n" +
            "    int idx = o.index(&x, &y);\n" +
            "    std::string a=\"l\", b=\"L\";\n" +
            "    std::string* rep = o.replace(&x, &a, &b);\n" +
            "    printf(\"upper=%s contains=%d index=%d replaced=%s\\n\", r->c_str(), c, idx, rep->c_str());\n" +
            "    return (std::string(*r)==\"HELLO WORLD\" && c && idx==6 && std::string(*rep)==\"HeLLo WorLd\") ? 0 : 1;\n" +
            "}\n";

    @Test
    public void stringMappersBehave() throws Exception {
        Path compiler = CppTestSupport.compiler();

        Path out = CppTestSupport.convert(JAVA, "s", "Str", "str");

        CppTestSupport.compile(out, compiler, "src/s/Str.cpp", MAIN, "str_test")
                .assertOk("str");
        CppTestSupport.runMerged(out.resolve("str_test")).assertOk("str");
    }
}
