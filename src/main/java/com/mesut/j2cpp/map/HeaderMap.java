package com.mesut.j2cpp.map;

import com.mesut.j2cpp.ast.CClass;
import com.mesut.j2cpp.ast.CHeader;
import com.mesut.j2cpp.ast.CType;

import java.util.HashMap;
import java.util.Map;

public class HeaderMap {
    public static HeaderMap instance = new HeaderMap();
    Map<CClass, CHeader> map = new HashMap<>();

    public CHeader getHeader(CClass cc) {
        if (map.containsKey(cc)) {
            return map.get(cc);
        }
        CHeader header = new CHeader(cc.getHeaderPath());
        return header;
    }

    public CHeader getHeader(CType cc) {
        return map.get(ClassMap.sourceMap.get(cc));
    }
}
