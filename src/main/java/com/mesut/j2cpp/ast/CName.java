package com.mesut.j2cpp.ast;

import com.mesut.j2cpp.cppast.CExpression;
import com.mesut.j2cpp.util.PrintHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//var name,method name,param name,class name
public class CName extends CExpression {

    public Namespace namespace;//for class names
    public String name;
    public List<CType> typeArgs = new ArrayList<>();//template method call

    public CName(String name) {
        String[] arr = name.split("::");
        if (arr.length > 1) {
            namespace = new Namespace(Arrays.asList(arr));
        }
        this.name = arr[arr.length - 1];
    }

    public static CName from(String name) {
        return new CName(name);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        getScope(typeArgs);
        sb.append(name);
        if (!typeArgs.isEmpty()) {
            sb.append("<").append(PrintHelper.joinStr(typeArgs, ", ")).append(">");
        }
        return sb.toString();
    }
}
