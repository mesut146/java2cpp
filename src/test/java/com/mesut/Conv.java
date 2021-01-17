package com.mesut;

import com.mesut.j2cpp.Converter;
import org.junit.Test;

public class Conv {
    public static String rtJar = "/home/mesut/Desktop/j2cpp-dev/rt7.jar";


    @Test
    public void testRt() {
        String dir = "/home/mesut/Desktop/j2cpp-dev/src7";
        String out = "/home/mesut/Desktop/j2cpp-dev/src7-cpp";
        Converter converter = new Converter(dir, out);
        //converter.addClasspath(dir);
        //converter.getFilter().addIncludeDir("java.lang");
        //converter.getFilter().addIncludeClass("java.lang.CharacterData01.java");
        //converter.setDebugMembers(true);
        converter.convert();
    }

    @Test
    public void testDx() {
        String dir = "/home/mesut/Desktop/j2cpp-dev/dx-org";
        String out = "/home/mesut/Desktop/j2cpp-dev/dx-cpp";
        Converter converter = new Converter(dir, out);
        //converter.addClasspath(dir);
        //converter.getFilter().addIncludeDir("java.lang");
        //converter.getFilter().addIncludeClass("com/android/dx/io/instructions/InstructionCodec");
        //converter.getFilter().addIncludeClass("com/android/dx/command/findusages/FindUsages.java");
        //converter.setDebugMembers(true);
        converter.convert();
    }

    @Test
    public void test() {
        String dir = "/home/mesut/Desktop/j2cpp-dev/jdeps/java";
        String out = "/home/mesut/Desktop/j2cpp-dev/jdeps/conv";
        dir = "/home/mesut/Desktop/jdeps-cpp/java";
        out = "/home/mesut/Desktop/jdeps-cpp/cpp";
        Converter converter = new Converter(dir, out);
        converter.addClasspath(rtJar);
        converter.addClasspath("/home/mesut/Desktop/ide-dev/ide-sdk/javac-ide/nio.min/src");
        //converter.getFilter().setIncludeAll(false);
        //converter.getFilter().addIncludeClass("com/sun/tools/classfile/ClassFile");
        //converter.getFilter().addIncludeClass("com/sun/tools/jdeps/Profile");
        //converter.getFilter().addIncludeClass("com/sun/tools/classfile/ClassWriter");
        //converter.getFilter().addIncludeClass("com/sun/tools/classfile/ConstantPool");
        //converter.getFilter().addIncludeDir("com/sun/tools/classfile");
        converter.setDebugMembers(true);
        converter.convert();
    }

    @Test
    public void testMy() {
        String dir = "/home/mesut/Desktop/IdeaProjects/java2cpp/asd/test/java";
        String out = "/home/mesut/Desktop/IdeaProjects/java2cpp/asd/test/cpp";
        Converter converter = new Converter(dir, out);
        converter.addClasspath(rtJar);
        //converter.getFilter().addIncludeClass("base.InnerTest");
        //converter.getFilter().addIncludeClass("base.SuperTest");
        converter.getFilter().addIncludeClass("base.AnonyTest");
        //converter.getFilter().addIncludeClass("base.iface");
        converter.setDebugMembers(true);
        converter.convert();
    }


}
