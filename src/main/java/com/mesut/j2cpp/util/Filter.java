package com.mesut.j2cpp.util;

import com.mesut.j2cpp.Util;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Filter {
    boolean includeAll = true;
    List<PackageName> includeDirs = new ArrayList<>();
    List<String> excludeDirs = new ArrayList<>();
    List<String> includeClasses = new ArrayList<>();
    List<String> excludeClasses = new ArrayList<>();
    Path srcDir;


    public Filter(Path srcDir) {
        this.srcDir = srcDir;
    }

    public void addIncludeDir(String prefix) {
        includeAll = false;
        prefix = prefix.replace('.', '/');
        includeDirs.add(new PackageName(prefix));
    }

    public void addExcludeDir(String prefix) {
        includeAll = false;
        excludeDirs.add(prefix);
    }

    public void addIncludeClass(String name) {
        includeAll = false;
        name = Util.trimSuffix(name, ".java");
        includeClasses.add(name);
    }

    public void addExcludeClass(String name) {
        excludeClasses.add(name);
    }

    public void setIncludeAll(boolean flag) {
        this.includeAll = flag;
    }

    public boolean checkPath(Path file) {
        if (includeAll) {
            return true;
        }
        Path parent = file.getParent();
        for (PackageName name : includeDirs) {
            if (parent.endsWith(name.getString())) {
                return true;
            }
        }
        for (String ex : excludeDirs) {
            if (parent.endsWith(ex)) {
                return false;
            }
        }
        String clsName = srcDir.relativize(file).toString();
        //String clsName = Util.relative(file.getAbsolutePath(), srcDir);
        clsName = Util.trimSuffix(clsName, ".java");
        for (String cls : includeClasses) {
            cls = cls.replace(".", "/");
            if (clsName.endsWith(cls)) {
                return true;
            }
        }
        return false;
    }
}
