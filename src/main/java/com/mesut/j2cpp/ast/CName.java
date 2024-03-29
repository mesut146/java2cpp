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
    public String orgName;

    public CName(String name) {
        if (name.contains("::")) {
            String[] arr = name.split("::");
            this.orgName = arr[arr.length - 1];
            this.name = arr[arr.length - 1];
            if (arr.length > 1) {
                namespace = new Namespace(Arrays.asList(arr));
            }
        }
        else {
            orgName = name;
            this.name = name;
        }
    }

    public static CName simple(String s) {
        CName res = new CName("");
        res.name = s;
        return res;
    }

    public static CName from(String name) {
        return new CName(name);
    }

    public boolean is(String s) {
        if (orgName != null) {
            return orgName.equals(s);
        }
        return name.equals(s);
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
