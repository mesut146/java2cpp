package com.mesut.j2cpp.ast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Template {
    public List<CType> list = new ArrayList<>();

    public void add(CType typeName) {
        list.add(typeName);
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public List<CType> getList() {
        return list;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("template <");
        for (Iterator<CType> iterator = list.iterator(); iterator.hasNext(); ) {
            sb.append("typename ");
            sb.append(iterator.next());
            if (iterator.hasNext()) {
                sb.append(",");
            }
        }
        sb.append(">");
        return sb.toString();
    }
}
