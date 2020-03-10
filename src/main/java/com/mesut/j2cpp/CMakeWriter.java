package com.mesut.j2cpp;

import java.util.ArrayList;
import java.util.List;

public class CMakeWriter {

    public String projectName;
    public List<Target> targets = new ArrayList<>();

    public CMakeWriter(String projectName) {
        this.projectName = projectName;
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
        sb.append("cmake_minimum_required(VERSION 2.8.11)\n\n");
        sb.append("project(").append(projectName).append(")\n\n");

        for (Target target : targets) {
            sb.append("add_library(").append(target.name);
            sb.append(" ");
            sb.append(target.isShared ? "SHARED" : "STATIC");
            sb.append("\n");
            for (String src : target.sourceFiles) {
                sb.append(src);
                sb.append("\n");
            }
            sb.append(")\n");
            sb.append("target_include_directories(");
            sb.append(target.name).append("\n");
            sb.append("${CMAKE_SOURCE_DIR}\n");
            for (String dir : target.includeDirs) {
                sb.append(dir);
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
    }
}

