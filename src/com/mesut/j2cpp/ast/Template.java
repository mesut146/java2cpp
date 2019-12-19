package com.mesut.j2cpp.ast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Template {
    public List<TypeName> list=new ArrayList<>();

    public void add(TypeName typeName){
        list.add(typeName);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("template <");
        for (Iterator<TypeName> iterator=list.iterator();iterator.hasNext();){
            sb.append("typename ");
            sb.append(iterator.next());
            if (iterator.hasNext()){
                sb.append(",");
            }
        }
        sb.append(">");
        return sb.toString();
    }
}
