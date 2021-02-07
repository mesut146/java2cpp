package com.mesut.j2cpp.cppast;

import com.mesut.j2cpp.util.PrintHelper;

import java.util.List;

public class CMultiStatement extends CStatement {
    List<CStatement> list;

    public CMultiStatement(List<CStatement> list) {
        this.list = list;
    }

    @Override
    public String toString() {
        getScope(list);
        if (list.size() == 1) {
            return list.get(0).toString();
        }
        return PrintHelper.joinStr(list, "\n");
    }
}
