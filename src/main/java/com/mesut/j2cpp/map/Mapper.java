package com.mesut.j2cpp.map;

import com.mesut.j2cpp.IncludeStmt;
import com.mesut.j2cpp.Util;
import com.mesut.j2cpp.ast.CMethod;
import com.mesut.j2cpp.ast.CSource;
import com.mesut.j2cpp.ast.CType;
import com.mesut.j2cpp.cppast.CExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Mapper {

    Map<CType, ClassInfo> classMap;

    public Mapper() {
        classMap = new HashMap<>();
    }

    public void addMapper(String jsonPath) throws IOException {
        JSONObject cls = new JSONObject(Util.read(new File(jsonPath)));
        List<CType> fromTypes = new ArrayList<>();
        String names = cls.getString("name");
        String target = cls.getString("target");
        String include = cls.getString("include");

        ClassInfo info = new ClassInfo();
        info.target = new CType(target);
        for (String name : names.split(",")) {
            classMap.put(new CType(name), info);
        }

        JSONArray methods = getObjects(cls);
        for (int i = 0; i < methods.length(); i++) {
            JSONObject method = methods.getJSONObject(i);
            String methodName = cls.getString("name");
            String methodTarget = cls.getString("target");
        }
    }

    private JSONArray getObjects(JSONObject cls) {
        JSONArray methods = cls.getJSONArray("methods");
        return methods;
    }

    public CExpression mapMethod(MethodInvocation node) {
        return null;
    }

    public CType mapType(CType type, CSource source) {
        if (classMap.containsKey(type)) {
            ClassInfo info = classMap.get(type);
            if (info.includes != null) {
                for (String inc : info.includes) {
                    source.addInclude(new IncludeStmt(inc));
                }
            }
            return info.target;
        }
        return type;
    }


    String mapParamName(String name, CMethod method) {
        if (Util.isKeyword(name)) {
            name = name + "_renamed";
        }
        //todo add to map
        return name;
    }

    static class ClassInfo {
        CType target;
        List<String> includes;
        Map methods;
    }

    static class MethodInfo {
        String name;
        String targetName;
        List<String> args;
    }
}
