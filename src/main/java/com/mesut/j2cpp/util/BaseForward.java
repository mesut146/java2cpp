package com.mesut.j2cpp.util;

import com.mesut.j2cpp.ast.CClass;
import com.mesut.j2cpp.ast.CHeader;
import com.mesut.j2cpp.ast.CType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BaseForward {
    CHeader header;


    public BaseForward(CHeader header) {
        this.header = header;
    }


    public void sort() throws Exception {
        Set<CType> set = new HashSet<>();
        for (CClass cc : header.classes) {
            set.add(cc.getType());
            set.addAll(cc.base);
        }
        List<CType> list = new ArrayList<>(set);
        header.getScope(list);
        sort(list);

        System.out.println(list);
        isValid(list);
        //then place classes to header in sorted order
        List<CClass> classes = new ArrayList<>(header.classes);
        header.classes.clear();
        for (CType type : list) {
            //is type is local cls
            for (CClass cc : classes) {
                if (cc.getType().equals(type)) {
                    header.addClass(cc);
                    break;
                }
            }
        }
    }


    void sort(List<CType> list) {
        list.sort((o1, o2) -> {
            for (CClass cc : header.classes) {
                //if o1 =this and o2=super
                if (cc.getType().equals(o1) && cc.base.contains(o2)) {
                    return 1;
                }
                else if (cc.getType().equals(o2) && cc.base.contains(o1)) {
                    //if o2 =this and o1=super
                    return -1;
                }

            }
            return 0;
        });
    }

    void isValid(List<CType> list) throws Exception {
        for (int i = 0; i < list.size(); i++) {
            for (int j = i + 1; j < list.size(); j++) {
                CType o1 = list.get(i);
                CType o2 = list.get(j);
                for (CClass cc : header.classes) {
                    //if o1 =this and o2=super
                    if (cc.getType().equals(o1) && cc.base.contains(o2)) {
                        //if o2 =this and o1=super
                        throw new Exception("cyclic inheritance between: " + o1 + ", " + o2);
                    }

                }
            }
        }
    }


}
