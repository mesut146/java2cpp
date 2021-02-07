package com.mesut.j2cpp.map;

import com.mesut.j2cpp.ast.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//hold all classes and inheritance relation
public class ClassMap {
    public Map<CType, CClass> map = new HashMap<>();

    public void add(CClass cc) {
        CClass decl = get(cc.getType());
        decl.template = cc.template;
        decl.base.addAll(cc.base);
    }

    public void addAll(List<CClass> list) {
        for (CClass cc : list) {
            add(cc);
        }
    }

    public CClass get(CType type) {
        if (map.containsKey(type)) {
            return map.get(type);
        }
        CClass cc = new CClass(type);
        for (CType arg : type.typeNames) {
            cc.template.add(arg);
        }
        map.put(type, cc);
        return cc;
    }

}
