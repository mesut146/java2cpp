package com.mesut;

import com.mesut.j2cpp.Converter;
import org.junit.Test;

public class Conv {

    @Test
    public void test() {
        String dir = "/home/mesut/Desktop/j2cpp-dev/jdeps/java";
        String out = "/home/mesut/Desktop/j2cpp-dev/jdeps/src";
        Converter converter = new Converter(dir, out);
        converter.addClasspath("/home/mesut/Desktop/j2cpp-dev/rt7.jar");
        //converter.setDebugMembers(true);
        converter.convert();

    }
}
