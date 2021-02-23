package com.mesut.j2cpp;

import java.util.List;
import java.util.Objects;

public class IncludeStmt {
    public String str;
    public boolean isSys = false;
    public boolean isLib = false;

    public IncludeStmt(String str) {
        this.str = Util.trimSuffix(str, ".h");
        if (str.startsWith("<")) {
            isSys = true;
            this.str = str.substring(1, str.length() - 1);
        }
    }

    public static void sort(List<IncludeStmt> list) {
        list.sort((i1, i2) -> {
            if (i1.isSys) return -1;
            if (i2.isSys) return 1;
            if (i1.isLib) {
                if (i2.isLib){
                    return i1.str.compareTo(i2.str);
                }
                return -1;
            }
            if (i2.isLib) return 1;
            return 0;
        });
    }

    public static IncludeStmt lib(String str) {
        IncludeStmt stmt = new IncludeStmt(str);
        stmt.isLib = true;
        return stmt;
    }

    public static IncludeStmt sys(String str) {
        IncludeStmt stmt = new IncludeStmt(str);
        stmt.isSys = true;
        return stmt;
    }

    public static IncludeStmt src(String str) {
        IncludeStmt stmt = new IncludeStmt(str);
        stmt.isSys = false;
        return stmt;
    }

    @Override
    public String toString() {
        if (isSys) {
            return "#include <" + str + ">";
        }
        return "#include \"" + str + ".h\"";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IncludeStmt that = (IncludeStmt) o;

        if (isSys != that.isSys) return false;
        return Objects.equals(str, that.str);
    }

    @Override
    public int hashCode() {
        int result = str != null ? str.hashCode() : 0;
        result = 31 * result + (isSys ? 1 : 0);
        return result;
    }
}
