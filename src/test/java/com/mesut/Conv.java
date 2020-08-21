package com.mesut;

import com.mesut.j2cpp.Converter;
import org.junit.Test;

public class Conv {


    String rtJar = "/home/mesut/Desktop/j2cpp-dev/rt7.jar";

    @Test
    public void testRt() {
        String dir = "/home/mesut/Desktop/j2cpp-dev/src7";
        String out = "/home/mesut/Desktop/j2cpp-dev/src7-cpp";
        Converter converter = new Converter(dir, out);
        //converter.addClasspath(rtJar);
        //converter.setDebugMembers(true);
        converter.convert();
    }

    @Test
    public void test() {
        String dir = "/home/mesut/Desktop/j2cpp-dev/jdeps/java";
        String out = "/home/mesut/Desktop/j2cpp-dev/jdeps/conv";
        Converter converter = new Converter(dir, out);
        converter.addClasspath(rtJar);
        //converter.getFilter().setIncludeAll(false);
        converter.getFilter().addIncludeClass("com/sun/tools/classfile/TypeAnnotation");
        //converter.getFilter().addIncludeClass("com/sun/tools/classfile/AccessFlags");
        //converter.getFilter().addIncludeDir("com/sun/tools/classfile");
        //converter.setDebugMembers(true);
        converter.convert();
    }
}
