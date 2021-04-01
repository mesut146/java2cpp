package com.mesut.j2cpp;

import com.mesut.j2cpp.ast.CType;
import com.mesut.j2cpp.util.PrintHelper;

import java.util.ArrayList;
import java.util.List;

public class IncludeList {
    List<IncludeStmt> list = new ArrayList<>();

    public void add(IncludeStmt stmt) {
        if (!list.contains(stmt)) {
            list.add(stmt);
        }
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

    public void remove(IncludeStmt stmt) {
        list.remove(stmt);
    }

    public void add(int i, IncludeStmt src) {
        list.add(i, src);
    }
}
