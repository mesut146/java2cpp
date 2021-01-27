package com.mesut.j2cpp.util;

import com.mesut.j2cpp.ast.CClass;
import com.mesut.j2cpp.ast.CHeader;
import com.mesut.j2cpp.ast.CType;

import java.util.*;

public class BaseForward {
    CHeader header;


    public BaseForward(CHeader header) {
        this.header = header;
    }


    public void sort() throws Exception {
        Set<CClass> set = new HashSet<>(header.classes);
        List<CClass> list = new ArrayList<>(set);
        header.getScope(list);
        sort(list);

        isValid(list);
        //then place classes to header in sorted order
        header.classes = list;
    }
    
    void sort(List<CClass> list) {
        list.sort((cur, base) -> {
            if (cur.base.contains(base.getType())) {
                return 1;//base must come before me
            }
            else if (base.base.contains(cur.getType())) {
                return -1;//we must come after base
            }
            return 0;//unrelated
        });
    }

    void isValid(List<CClass> list) throws Exception {
        for (int i = 0; i < list.size(); i++) {
            CClass base = list.get(i);
            for (int j = i + 1; j < list.size(); j++) {
                CClass cur = list.get(j);
                if (base.base.contains(cur)) {
                    throw new Exception("cyclic inheritance between: " + base + ", " + cur);
                }
            }
        }
    }


}
