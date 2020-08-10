package com.mesut.j2cpp.util;

import com.mesut.j2cpp.PackageName;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Filter {
    boolean includeAll = true;
    List<PackageName> includeDirs = new ArrayList<>();
    List<String> excludeDirs = new ArrayList<>();
    List<String> includeClasses = new ArrayList<>();
    List<String> excludeClasses = new ArrayList<>();

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
        includeClasses.add(name);
    }

    public void addExcludeClass(String name) {
        excludeClasses.add(name);
    }

    public void setIncludeAll(boolean flag) {
        this.includeAll = flag;
    }

    public boolean checkPath(File file) {
        if (includeAll) {
            return true;
        }
        String parent = file.getParent();
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
        return false;
    }
}
