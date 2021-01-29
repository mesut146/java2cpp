package com.mesut.j2cpp;

import com.mesut.j2cpp.ast.CType;
import com.mesut.j2cpp.cppast.CExpression;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class Mapper {

    Map<String, String> classMap;

    public void addMapper(String jsonPath) throws IOException {
        JSONObject cls = new JSONObject(Util.read(new File(jsonPath)));
        String name = cls.getString("name");
        String target = cls.getString("target");
        String include = cls.getString("include");
        JSONArray methods = cls.getJSONArray("methods");
        for (int i = 0; i < methods.length(); i++) {
            JSONObject method = methods.getJSONObject(i);
            String methodName = cls.getString("name");
            String methodTarget = cls.getString("target");
        }
    }

    CExpression mapMethod() {
        return null;
    }

    CType mapType() {
        return null;
    }

    static class ClassInfo {
        String name;
        Map methods;
    }

    static class MethodInfo {
        String name;
        String targetName;
        List<String> args;
    }
}
