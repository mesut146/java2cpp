package com.mesut.j2cpp.util;

import com.mesut.j2cpp.IncludeStmt;
import com.mesut.j2cpp.ast.CClass;
import com.mesut.j2cpp.ast.CSource;
import com.mesut.j2cpp.ast.CType;
import com.mesut.j2cpp.map.ClassMap;

import javax.sound.midi.spi.SoundbankReader;
import java.util.*;

public class BaseClassSorter {

    public static void sort(List<CClass> classes) throws Exception {
        sort0(classes);
        isValid(classes);
    }

    static void sort0(List<CClass> list) {
        list.sort((c1, c2) -> {
            if (c1.superClass != null && c1.superClass.equals(c2.getType())) {
                return 1;
            }
            if (c2.superClass != null && c2.superClass.equals(c1.getType())) {
                return -1;
            }
            if (c1.ifaces.contains(c2.getType())) {
                return 1;
            }
            else if (c2.ifaces.contains(c1.getType())) {
                return -1;
            }
            return 0;//unrelated
        });
    }

    static void isValid(List<CClass> list) throws Exception {
        for (int i = 0; i < list.size(); i++) {
            CClass base = list.get(i);
            for (int j = i + 1; j < list.size(); j++) {
                CClass cur = list.get(j);
                if (base.ifaces.contains(cur)) {
                    throw new Exception("cyclic inheritance between: " + base + ", " + cur);
                }
            }
        }
    }


}
