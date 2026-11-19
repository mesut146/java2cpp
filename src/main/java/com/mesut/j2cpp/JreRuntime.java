package com.mesut.j2cpp;

import com.mesut.j2cpp.Config;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;

/** Hand-written Java runtime classes that replace generated stubs in -no-mappers mode. */
public final class JreRuntime {

    private static final String[] FILES = {
            "implemented.txt",
            "java/lang/Object.h", "java/lang/Number.h", "java/lang/Integer.h",
            "java/lang/Boolean.h", "java/lang/Long.h", "java/lang/Double.h",
            "java/lang/Float.h", "java/lang/Short.h", "java/lang/Byte.h",
            "java/lang/Character.h", "java/lang/String.h", "java/lang/StringBuilder.h",
            "java/lang/StringBuffer.h", "java/lang/Math.h", "java/lang/System.h",
            "java/lang/Throwable.h", "java/lang/Exception.h",
            "java/lang/RuntimeException.h", "java/lang/IllegalArgumentException.h",
            "java/lang/IllegalStateException.h", "java/lang/Error.h",
            "java/io/PrintStream.h",
            "java/util/List.h", "java/util/ArrayList.h", "java/util/Iterator.h", "java/util/Set.h",
            "java/util/HashSet.h", "java/util/Map.h", "java/util/HashMap.h",
            "java/util/Arrays.h", "java/util/Objects.h", "java/util/Optional.h",
            "java/util/Random.h", "java/util/UUID.h"
    };

    private static final Set<String> IMPLEMENTED = new LinkedHashSet<>();

    static {
        try (InputStream in = JreRuntime.class.getClassLoader().getResourceAsStream("jre/implemented.txt")) {
            if (in != null) {
                for (String line : new String(in.readAllBytes(), StandardCharsets.UTF_8).split("\\R")) {
                    if (!line.trim().isEmpty()) IMPLEMENTED.add(line.trim());
                }
            }
        }
        catch (IOException ignored) {
        }
    }

    private JreRuntime() {
    }

    public static boolean isImplemented(String binaryName) {
        return !Config.useMappers && binaryName != null && IMPLEMENTED.contains(binaryName);
    }

    public static String headerPath(String binaryName) {
        return "jre/" + binaryName.replace('.', '/') + ".h";
    }

    public static void copyTo(Path destDir) throws IOException {
        if (Config.useMappers) return;
        Path target = destDir.resolve("jre");
        Files.createDirectories(target);
        for (String file : FILES) {
            Path copy = target.resolve(file);
            try (InputStream in = JreRuntime.class.getClassLoader()
                    .getResourceAsStream("jre/" + file)) {
                if (in != null) {
                    Files.createDirectories(copy.getParent());
                    Files.copy(in, copy, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    continue;
                }
            }
            Path local = Path.of("jre", file);
            if (Files.isRegularFile(local)) {
                Files.createDirectories(copy.getParent());
                Files.copy(local, copy, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
            else {
                Logger.log("JRE runtime file not found: " + file);
            }
        }
    }

    //used by LibHandler: map stub type names to JRE class names
    public static String implementedName(com.mesut.j2cpp.ast.CType type) {
        if (type == null || type.realName == null) return null;
        return IMPLEMENTED.contains(type.realName) ? type.realName : null;
    }
}
