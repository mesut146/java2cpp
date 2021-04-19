package com.mesut.j2cpp.map;

import com.mesut.j2cpp.IncludeStmt;
import com.mesut.j2cpp.Logger;
import com.mesut.j2cpp.Util;
import com.mesut.j2cpp.ast.CClass;
import com.mesut.j2cpp.ast.CMethod;
import com.mesut.j2cpp.ast.CName;
import com.mesut.j2cpp.ast.CType;
import com.mesut.j2cpp.cppast.CExpression;
import com.mesut.j2cpp.visitor.TypeVisitor;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class Mapper {

    public static Mapper instance = new Mapper();
    Map<String, ClassInfo> classMap;//java class -> info

    public Mapper() {
        classMap = new HashMap<>();
    }

    static void parseSignature(MethodInfo methodInfo) {
        String sig = methodInfo.str;
        int parL = sig.indexOf("(");
        int parR = sig.lastIndexOf(")");
        String name = sig.substring(0, parL);
        String argStr = sig.substring(parL + 1, parR);
        if (!argStr.isEmpty()) {
            String[] args = argStr.split(",");
            for (String type : args) {
                methodInfo.args.add(parseType(type));
            }
        }
        methodInfo.name = name;
    }

    static CType parseType(String str) {
        if (str.startsWith("<")) {
            return new CType(str.substring(1, str.length() - 1), true);
        }
        if (str.endsWith("[]")) {

        }
        return new CType(str);
    }

    public static String map(String name) {
        return name + "_renamed";
    }

    public void initMappers() throws IOException {
        String[] all = {"list.json", "map.json", "set.json", "string.json", "Boolean.json", "Integer.json"};
        for (String mapper : all) {
            addMapper(getClass().getResourceAsStream("/mappers/" + mapper));
        }
    }

    public void addMapper(String jsonPath) throws IOException {
        addMapper(new FileInputStream(jsonPath));
    }

    public void addMapper(InputStream is) throws IOException {
        JSONObject cls = new JSONObject(Util.read(is));
        List<CType> fromTypes = new ArrayList<>();
        String target = cls.getString("target");

        ClassInfo info = new ClassInfo();
        info.target = new CType(target);
        for (String name : cls.getString("name").split(",")) {
            classMap.put(name, info);
        }
        String include = cls.optString("include", null);
        if (include != null) {
            info.includes.addAll(Arrays.asList(include.split(",")));
        }

        JSONArray methods = cls.getJSONArray("methods");
        for (int i = 0; i < methods.length(); i++) {
            JSONObject method = methods.getJSONObject(i);
            MethodInfo methodInfo = new MethodInfo();
            methodInfo.str = method.getString("name");
            if (method.get("target") instanceof JSONArray) {
                StringBuilder sb = new StringBuilder();
                for (Object o : method.getJSONArray("target")) {
                    sb.append(o).append("\n");
                }
                sb.setLength(sb.length() - 1);//trim last \n
                methodInfo.targetExpr = sb.toString();
            }
            else {
                methodInfo.targetExpr = method.getString("target");
            }
            methodInfo.warning = method.optString("warning", null);
            methodInfo.expr = method.optString("expr", null);
            methodInfo.external = method.optBoolean("external", false);
            String inc = method.optString("include", null);
            if (inc != null) {
                info.includes.addAll(Arrays.asList(inc.split(",")));
            }
            info.methods.add(methodInfo);
            parseSignature(methodInfo);
        }
    }

    public Mapped mapMethod(IMethodBinding binding, List<CExpression> args, CExpression scope) {
        CType type = TypeVisitor.fromBinding(binding.getDeclaringClass());
        ClassInfo classInfo = classMap.get(type.realName);
        if (classInfo == null) return null;//no mapping for this type
        MethodInfo info = findMethod(classInfo, binding);
        if (info == null) {
            //no mapping
            Logger.log("missing mapper for " + binding.getDeclaringClass().getQualifiedName() + " -> " + binding);
            return null;
        }
        //replace
        String e = info.targetExpr;
        //args
        for (int i = 0; i < args.size(); i++) {
            e = e.replace("$" + (i + 1), args.get(i).toString());
        }
        e = e.replace("${varName}", scope.toString());//todo put in variable maybe?
        Mapped mapped = new Mapped();
        if (info.expr != null) {
            //multi statement
            mapped.expr = CName.simple(info.expr);
            mapped.list = e;
        }
        else {
            //with scope
            if (!info.external) {
                e = scope + "->" + e;
            }
            mapped.expr = CName.simple(e);
        }
        if (info.warning != null) {
            Logger.log(info.warning);
        }
        return mapped;
    }

    MethodInfo findMethod(ClassInfo classInfo, IMethodBinding binding) {
        IMethodBinding real = binding.getMethodDeclaration();
        Map<CType, Integer> order = new HashMap<>();

        for (MethodInfo info : classInfo.methods) {
            if (!info.name.equals(binding.getName())) continue;
            if (info.args.size() != binding.getParameterTypes().length) continue;
            boolean found = true;
            for (int i = 0; i < binding.getParameterTypes().length; i++) {
                CType t1 = info.args.get(i);
                ITypeBinding t2 = binding.getParameterTypes()[i];
                ITypeBinding t3 = real.getParameterTypes()[i];
                if (t1.isTemplate) {
                    if (t3.isTypeVariable()) {
                        //save?
                    }
                    else {
                        found = false;
                        break;
                    }
                }
                else {
                    if (!t1.realName.equals(t2.getQualifiedName())) {
                        found = false;
                        break;
                    }
                }
            }
            if (found) {
                return info;
            }
        }
        return null;
    }

    public CType mapType(CType type, CClass cc) {
        if (classMap.containsKey(type.realName)) {
            ClassInfo info = classMap.get(type.realName);
            if (info.includes != null) {
                if (cc != null)
                    for (String inc : info.includes) {
                        cc.includes.add(new IncludeStmt(inc));
                    }
            }
            CType target = info.target.copy();
            target.typeNames = type.typeNames;
            target.realName = type.realName;
            target.mapped = true;
            return target;
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

    public String mapName(String name) {
        if (Util.isKeyword(name)) {
            name = map(name);
        }
        //todo add to map
        return name;
    }

    public CName mapFieldName(String name, CClass cc) {
        String org = name;
        CName res;
        if (Util.isKeyword(name)) {
            name = map(name);
        }
        else {
            for (CMethod method : cc.methods) {
                if (method.name.is(name)) {
                    name = map(name);
                    break;
                }
            }
        }

        res = new CName(name);
        res.orgName = org;
        return res;
    }

    public static class Mapped {
        public String list;
        public CExpression expr;
    }

    static class ClassInfo {
        CType target;
        List<String> includes = new ArrayList<>();
        List<MethodInfo> methods = new ArrayList<>();
    }

    static class MethodInfo {
        String name;
        String str;
        String targetExpr;
        String warning;
        boolean external = false;
        boolean multi = false;
        String expr;
        List<CType> args = new ArrayList<>();
        List<String> includes = new ArrayList<>();
    }
}
