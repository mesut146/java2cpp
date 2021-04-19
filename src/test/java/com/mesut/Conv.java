package com.mesut;

import com.mesut.j2cpp.Converter;
import com.mesut.j2cpp.map.Mapper;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Conv {
    public static String rtJar = "/home/mesut/Desktop/j2cpp-dev/rt7.jar";

    @Test
    public void testList() throws URISyntaxException, IOException {
        String dir = "/home/mesut/Desktop/j2cpp-dev/src7";
        String out = "/home/mesut/Desktop/j2cpp-dev/src7-cpp";
        Converter converter = new Converter(dir, out);
        for (String line : Files.readAllLines(Paths.get(getClass().getResource("/list").toURI()))) {
            converter.getFilter().addIncludeClass(line);
        }
        //converter.getFilter().addIncludeClass("java/util/Collections.java");
        converter.convert();
    }

    @Test
    public void testRt() {
        String dir = "/home/mesut/Desktop/j2cpp-dev/src7";
        String out = "/home/mesut/Desktop/j2cpp-dev/src7-cpp";
        Converter converter = new Converter(dir, out);
        //converter.getFilter().addIncludeDir("java.lang");
        //converter.getFilter().addIncludeClass("java.lang.CharacterData01.java");
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
        converter.convert();
    }

    @Test
    public void testDx() throws IOException {
        String dir = "/home/mesut/Desktop/j2cpp-dev/dx-org";
        //String out = "/home/mesut/Desktop/j2cpp-dev/dx-cpp";
        String out = "/home/mesut/Desktop/j2cpp-dev/dx2";
        Converter converter = new Converter(dir, out);
        converter.addClasspath(rtJar);
        Mapper.instance.initMappers();
        //converter.getFilter().addIncludeClass("com/android/dex/Dex");
        //converter.getFilter().addIncludeClass("com/android/dx/rop/code/Insn.java");
        //converter.getFilter().addIncludeClass("com/android/dx/util/Output.java");
        //converter.getFilter().addIncludeClass("com/android/dx/dex/code/PositionList.java");
        //converter.getFilter().addIncludeClass("com/android/dx/dex/cf/CfOptions");
        converter.convert();
    }

    @Test
    public void testMy() throws IOException {
        String dir = "/home/mesut/Desktop/IdeaProjects/java2cpp/asd/test/java";
        String out = "/home/mesut/Desktop/IdeaProjects/java2cpp/asd/test/cpp";
        Converter converter = new Converter(dir, out);
        converter.addClasspath(rtJar);
        Mapper.instance.initMappers();
        //Config.common_forwards = false;
        //converter.getFilter().addIncludeClass("base.InnerTest");
        //converter.getFilter().addIncludeClass("base.SuperTest");
        //converter.getFilter().addIncludeClass("base.iface");
        //converter.getFilter().addIncludeClass("base.ArrayTest");
        //converter.getFilter().addIncludeClass("base.Inner2Test");
        //converter.getFilter().addIncludeClass("base.Generic");
        //converter.getFilter().addIncludeClass("base.Fields");
        //converter.getFilter().addIncludeClass("base.Try1");
        //converter.getFilter().addIncludeClass("base.Try2");
        converter.getFilter().addIncludeClass("base.SwitchTest");
        //converter.getFilter().addIncludeClass("base.Enum1");
        //converter.getFilter().addIncludeClass("base.StringTest");
        converter.convert();
    }

    @Test
    public void testMapper() throws IOException {
        String dir = "/home/mesut/Desktop/IdeaProjects/java2cpp/asd/test/java";
        String out = "/home/mesut/Desktop/IdeaProjects/java2cpp/asd/test/cpp";
        Converter converter = new Converter(dir, out);
        Mapper.instance.initMappers();
        converter.addClasspath(rtJar);
        //converter.getFilter().addIncludeClass("mapper.ListTest");
        //converter.getFilter().addIncludeClass("mapper.StringTest");
        converter.getFilter().addIncludeClass("mapper.SetTest");
        //converter.getFilter().addIncludeClass("mapper.IntegerTest");
        converter.convert();
    }

    @Test
    public void asd() {
        name(6);
    }

    int aa() {
        return 1;
    }

    public void name(int y) {
        switch (aa()) {
            case 1: {
                if (y % 2 == 0) {
                    break;
                }
                System.out.println("1");
                break;
            }
            case 2:
                System.out.println("2");
            default:
                System.out.println("def");
        }
    }

}
