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
        converter.addClasspath("/home/mesut/Desktop/ide-dev/ide-sdk/javac-ide/nio.min/src");
        //converter.getFilter().setIncludeAll(false);
        //converter.getFilter().addIncludeClass("com/sun/tools/classfile/ClassFile");
        //converter.getFilter().addIncludeClass("com/sun/tools/jdeps/Profile");
        //converter.getFilter().addIncludeClass("com/sun/tools/classfile/ClassWriter");
        //converter.getFilter().addIncludeClass("com/sun/tools/classfile/ConstantPool");
        //converter.getFilter().addIncludeDir("com/sun/tools/classfile");
        //converter.setDebugMembers(true);
        converter.convert();
    }

    @Test
    public void tt() {
        System.out.println(asd());
    }

    static int asd() {
        try {
            return inner1();
        } finally {
            inner2();
        }
    }

    static int inner1() {
        System.out.println("inner1");
        throw new RuntimeException("haha");
        //return 5;
    }

    static int inner2() {
        System.out.println("inner2");
        return 10;
    }
}
