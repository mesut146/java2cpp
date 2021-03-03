package com.mesut.j2cpp.util;

import com.mesut.j2cpp.ast.CClass;

import java.util.*;

public class BaseClassSorter {

    public static void sort(List<CClass> classes) throws Exception {
        //sort0(classes);
        sortBubble(classes);
        isValid(classes);
    }

    static void sortBubble(List<CClass> list) {
        while (true) {
            boolean any = false;
            for (int i = 0; i < list.size() - 1; i++) {
                for (int j = i + 1; j < list.size(); j++) {
                    CClass c1 = list.get(i);
                    CClass c2 = list.get(j);
                    if (isDep(c1, c2)) {
                        //swap
                        list.set(j, c1);
                        list.set(i, c2);
                        any = true;
                    }
                }
            }
            if (!any) {
                break;
            }
        }
    }

    static void isValid(List<CClass> list) throws Exception {
        for (int i = 0; i < list.size(); i++) {
            CClass base = list.get(i);
            for (int j = i + 1; j < list.size(); j++) {
                CClass cur = list.get(j);
                if (isDep(base, cur)) {
                    throw new Exception("cyclic inheritance between: " + base.getType() + ", " + cur.getType());
                }
            }
        }
    }

    static boolean isDep(CClass c1, CClass c2) {
        if (c1.superClass != null && c1.superClass.equals(c2.getType())) {
            return true;
        }
        return c1.ifaces.contains(c2.getType());
    }


}
