package com.mesut.j2cpp;

import com.mesut.j2cpp.ast.CClass;
import com.mesut.j2cpp.ast.CType;
import com.mesut.j2cpp.ast.Template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//hold all classes and inheritance relation
public class ClassMap {

    Map<String, ClassDecl> map = new HashMap<>();

    public void add(CClass cc) {
        ClassDecl decl = get(cc.getType());
        decl.template = cc.template;
        for (CType base : cc.base) {
            decl.base.add(get(base));
        }
    }

    public void addAll(List<CClass> list) {
        for (CClass cc : list) {
            add(cc);
        }
    }

    public ClassDecl get(CType type) {
        if (map.containsKey(type.basicForm())) {
            return map.get(type.basicForm());
        }
        ClassDecl decl = new ClassDecl(type);
        map.put(type.basicForm(), decl);
        return decl;
    }

    //single class decl
    public static class ClassDecl {
        public Template template;
        CType type;
        List<ClassDecl> base = new ArrayList<>();

        public ClassDecl(CType type) {
            this.type = type;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            if (template != null && !template.isEmpty()) {
                sb.append(template).append("\n");
            }
            sb.append("class ");
            sb.append(type.getName());
            sb.append(";");
            return sb.toString();
        }
    }

}
