package com.mesut;

import com.mesut.j2cpp.Converter;
import com.mesut.j2cpp.map.Mapper;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

/**
 * Conversion smoke tests over the hand-written samples in src/test/resources/samples.
 * Outputs go to temporary directories so tests are independent of the machine.
 */
public class ConversionTest {

    private Path sampleRoot() throws URISyntaxException {
        return Paths.get(getClass().getResource("/samples/").toURI());
    }

    private Path tempOut() throws IOException {
        return Files.createTempDirectory("j2cpp-conv-out");
    }

    @Test
    public void convertsSwitchSample() throws Exception {
        Path out = tempOut();
        Converter converter = new Converter(sampleRoot().toString(), out.toString());
        converter.getFilter().addIncludeClass("base.SwitchTest");
        converter.convert();
        assertTrue(Files.exists(out.resolve("base/SwitchTest.h")));
        assertTrue(Files.exists(out.resolve("src/base/SwitchTest.cpp")));
    }

    @Test
    public void convertsListSampleWithCppMappers() throws Exception {
        Path out = tempOut();
        Mapper.instance.initMappers();
        Converter converter = new Converter(sampleRoot().toString(), out.toString());
        converter.getFilter().addIncludeClass("mapper.ListTest");
        converter.convert();
        Path source = out.resolve("src/mapper/ListTest.cpp");
        assertTrue(Files.exists(source));
        String cpp = new String(Files.readAllBytes(source), StandardCharsets.UTF_8);
        assertTrue("List.add was not mapped: " + cpp, cpp.contains("push_back"));
    }

    @Test
    public void collectsStats() throws Exception {
        Path out = tempOut();
        Converter converter = new Converter(sampleRoot().toString(), out.toString());
        converter.getFilter().addIncludeClass("base.Ops");
        converter.stats();
        assertTrue(Files.exists(out.resolve("stats.txt")));
    }
}
