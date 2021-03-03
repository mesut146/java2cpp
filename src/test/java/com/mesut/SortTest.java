package com.mesut;

import com.mesut.j2cpp.ast.CClass;
import com.mesut.j2cpp.ast.CHeader;
import com.mesut.j2cpp.ast.CType;
import com.mesut.j2cpp.util.BaseClassSorter;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Stack;

public class SortTest {

    @Test
    public void test() throws Exception {
        CClass a = makeClass("a");
        CClass b = makeClass("b");
        CClass c = makeClass("c");
        CClass d = makeClass("d");

        a.ifaces.add(b.getType());
        c.ifaces.add(d.getType());

        List<CClass> classes = Arrays.asList(a, b, c, d);
        BaseClassSorter.sort(classes);
        for (CClass cc : classes) {
            System.out.println(cc.getType());
        }
    }

    @Test
    public void test2() throws Exception {
        CClass a = makeClass("AnnotatedOutput");
        CClass b = makeClass("Output");
        CClass c = makeClass("ByteOutput");

        a.ifaces.add(b.getType());
        b.ifaces.add(c.getType());

        List<CClass> classes = Arrays.asList(b, a, c);
        BaseClassSorter.sort(classes);
        for (CClass cc : classes) {
            System.out.println(cc.getType());
        }
    }

    CClass makeClass(String name) {
        return new CClass(new CType(name));
    }
}
