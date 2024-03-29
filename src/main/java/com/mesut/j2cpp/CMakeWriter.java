package com.mesut.j2cpp;

import java.util.ArrayList;
import java.util.List;

public class CMakeWriter {

    static String version = "2.8.11";
    public String projectName;
    public List<Target> targets = new ArrayList<>();
    public String sourceDir;

    public CMakeWriter(String projectName) {
        this.projectName = projectName;
    }

    public static String relative(String file, String root) {
        if (file.equals(root)) {
            return "";
        }
        if (file.startsWith(root)) {
            return file.substring(root.length() + 1);
        }
        return file;
    }

    public Target addTarget(String name, boolean isShared) {
        Target target = new Target();
        target.name = name;
        target.isShared = isShared;
        targets.add(target);
        return target;
    }

    public String generate() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("cmake_minimum_required(VERSION %s)\n\n", version));
        sb.append(String.format("project(%s)\n\n", projectName));

        for (Target target : targets) {
            sb.append("add_library(").append(target.name);
            sb.append(" ");
            sb.append(target.isShared ? "SHARED" : "STATIC");
            sb.append("\n");
            for (String src : target.sourceFiles) {
                sb.append("  ");//indention
                sb.append(relative(src, sourceDir));
                sb.append("\n");
            }
            sb.append(")\n");
            sb.append("include_directories(");
            if (!target.includeDirs.isEmpty()) {
                sb.append("\n");
            }
            for (String dir : target.includeDirs) {
                sb.append("  ");//indent
                sb.append("${CMAKE_SOURCE_DIR}");
                String path = relative(dir, sourceDir);
                if (!path.isEmpty()) {
                    sb.append("/");
                    sb.append(path);
                }
                sb.append("\n");
            }
            sb.append(")\n");
        }
        return sb.toString();
    }

    //static/shared library
    public static class Target {
        public boolean isShared = false;
        public String name;
        public List<String> sourceFiles = new ArrayList<>();
        public List<String> includeDirs = new ArrayList<>();

        public void addInclude(String dir) {
            if (!includeDirs.contains(dir)) {
                includeDirs.add(dir);
            }
        }
    }
}

