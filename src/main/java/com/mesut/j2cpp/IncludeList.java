package com.mesut.j2cpp;

import com.mesut.j2cpp.ast.CType;
import com.mesut.j2cpp.util.PrintHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class IncludeList {
    Set<IncludeStmt> list = new HashSet<>();

    public void add(IncludeStmt stmt) {
        list.add(stmt);
    }

    public void add(String stmt) {
        add(new IncludeStmt(stmt));
    }

    public void add(CType stmt) {
        add(stmt.basicForm().replace("::", "/"));
    }

    @Override
    public String toString() {
        List<IncludeStmt> ll = new ArrayList<>(list);
        IncludeStmt.sort(ll);
        return PrintHelper.joinStr(ll, "\n");
    }
}
