package com.mesut.j2cpp;

import com.mesut.j2cpp.ast.CType;
import com.mesut.j2cpp.util.PrintHelper;
import com.mesut.j2cpp.util.TypeHelper;

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

    public void add(CType type) {
        if (type.isPrim() || type.isVoid()) return;
        if (type.isTemplate) return;
        if (type.equals(TypeHelper.getVectorType())) {
            add(IncludeStmt.sys("vector"));
            return;
        }
        //can be local class,those already included by converter so ignore
        IncludeStmt stmt = new IncludeStmt(type.basicForm().replace("::", "/"));
        if (type.fromSource) {
            stmt.isSys = false;
        }
        else {
            stmt.isLib = true;
        }
        add(stmt);
    }

    @Override
    public String toString() {
        if (list.isEmpty()) {
            return "";
        }
        List<IncludeStmt> ll = new ArrayList<>(list);
        IncludeStmt.sort(ll);
        return PrintHelper.joinStr(ll, "\n") + "\n";
    }

    public void remove(IncludeStmt stmt) {
        list.remove(stmt);
    }

    public void add(int i, IncludeStmt src) {
        list.add(i, src);
    }
}
