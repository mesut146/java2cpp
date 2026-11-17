package com.mesut.j2cpp;

import java.io.IOException;

/**
 * Entry point for build configurations that reference com.mesut.j2cpp.Main.
 * All argument handling lives in {@link Cmd}.
 */
public class Main {

    public static void main(String[] args) throws IOException {
        Cmd.main(args);
    }
}
