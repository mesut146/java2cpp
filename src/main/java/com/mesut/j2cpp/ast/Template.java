package com.mesut.j2cpp.ast;

import com.mesut.j2cpp.cppast.CNode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

//template <class A,class B,...>
public class Template extends CNode {
    public static boolean useTypeName = true;//or class
    public List<CType> names = new ArrayList<>();

    public void add(CType typeName) {
        names.add(typeName);
    }

    public boolean isEmpty() {
        return names.isEmpty();
    }

    public List<CType> getNames() {
        return names;
    }

    @Override
    public String toString() {
        getScope(names);
        StringBuilder sb = new StringBuilder();
        sb.append("template <");
        for (Iterator<CType> iterator = names.iterator(); iterator.hasNext(); ) {
            if (useTypeName) {
                sb.append("typename ");
            }
            else {
                sb.append("class ");
            }
            sb.append(iterator.next());
            if (iterator.hasNext()) {
                sb.append(", ");
            }
        }
        sb.append(">");
        return sb.toString();
    }
}
