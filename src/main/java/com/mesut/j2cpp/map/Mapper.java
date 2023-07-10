package com.mesut.j2cpp.map;

import com.mesut.j2cpp.IncludeStmt;
import com.mesut.j2cpp.Logger;
import com.mesut.j2cpp.Util;
import com.mesut.j2cpp.ast.CClass;
import com.mesut.j2cpp.ast.CMethod;
import com.mesut.j2cpp.ast.CName;
import com.mesut.j2cpp.ast.CType;
import com.mesut.j2cpp.visitor.Rust;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Name;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Pattern;

public class Mapper {

    public static Mapper instance = new Mapper();
    Map<String, ClassInfo> classMap;//java class -> info

    public Mapper() {
        classMap = new HashMap<>();
    }

    static void parseSignature(MethodInfo methodInfo, String sig) {
        int parL = sig.indexOf("(");
        int parR = sig.lastIndexOf(")");
        String name = sig.substring(0, parL);
        String argStr = sig.substring(parL + 1, parR);
        if (!argStr.isEmpty()) {
            String[] args = argStr.split(",");
            methodInfo.args.addAll(Arrays.asList(args));
        }
        methodInfo.name = name;
    }

    public static String map(String name) {
        return name + "_renamed";
    }

    public void initMappers() throws IOException {
        initMappers(false);
    }

    public void initMappers(boolean rust) throws IOException {
        if (rust) {
            String[] all = {"list.json", "set.json", "string-builder.json", "string.json", "map.json"};
            for (String mapper : all) {
                addMapper(getClass().getResourceAsStream("/rust/" + mapper));
            }
        }
        else {
            //String[] all = {"list.json", "map.json", "set.json", "string.json", "Boolean.json", "Integer.json"};
            String[] all = {"list.json", "string.json", "Boolean.json"};
            for (String mapper : all) {
                addMapper(getClass().getResourceAsStream("/cpp/" + mapper));
            }
        }
    }

    public void addMapper(String jsonPath) throws IOException {
        addMapper(new FileInputStream(jsonPath));
    }

    public void addMapper(InputStream is) throws IOException {
        JSONObject cls = new JSONObject(Util.read(is));
        String target = cls.getString("target");

        ClassInfo info = new ClassInfo();
        info.target = CType.parse(target);

        for (String name : cls.getString("name").split(" ")) {
            var type = JavaType.parse(name, info);
            if (info.typeVars.isEmpty()) {
                info.typeVars.addAll(type.typeVars);
            }
            classMap.put(type.name, info);
        }
        String include = cls.optString("include", null);
        if (include != null) {
            info.includes.addAll(Arrays.asList(include.split(",")));
        }

        if (cls.has("fields")) {
            JSONArray farr = cls.getJSONArray("fields");
            for (int i = 0; i < farr.length(); i++) {
                JSONObject field = farr.getJSONObject(i);
                FieldInfo fieldInfo = new FieldInfo();
                fieldInfo.name = field.getString("name");
                fieldInfo.target = field.getString("target");
                fieldInfo.external = field.optBoolean("external", false);
            }
        }

        JSONArray methods = cls.getJSONArray("methods");
        for (int i = 0; i < methods.length(); i++) {
            JSONObject method = methods.getJSONObject(i);
            MethodInfo methodInfo = new MethodInfo();
            parseSignature(methodInfo, method.getString("name"));
            if (method.get("target") instanceof JSONArray) {
                for (Object o : method.getJSONArray("target")) {
                    methodInfo.target.add(o.toString());
                }
            }
            else {
                methodInfo.target.add(method.getString("target"));
            }
            methodInfo.warning = method.optString("warning", null);
            String inc = method.optString("include", null);
            if (inc != null) {
                methodInfo.includes.addAll(Arrays.asList(inc.split(",")));
            }
            info.methods.add(methodInfo);
        }
    }

    String toStr(Expression expr, ITypeBinding binding) {
        var visitor = new Rust(null, null);
        visitor.binding = binding;
        expr.accept(visitor);
        return visitor.code.toString();
    }

    public MethodInfo findInfo(IMethodBinding binding) {
        ClassInfo classInfo = classMap.get(binding.getDeclaringClass().getBinaryName());
        if (classInfo == null) return null;//no mapping for this type
        return findMethod(classInfo, binding);
    }

    public List<String> mapMethod(IMethodBinding binding, List<Expression> args, Expression scope, ITypeBinding cc) {
        MethodInfo info = findInfo(binding);
        if (info == null) {
            //no mapping
            Logger.log(String.format("missing mapper in '%s' method='%s'", cc, binding));
            return null;
        }
        //count var appearance
        List<String> result = new ArrayList<>();
        int varCnt = 0;
        for (var line : info.target) {
            var pat = Pattern.compile("\\$0");
            var m = pat.matcher(line);
            while (m.find()) {
                varCnt++;
            }
        }
        String scopeVar = "";
        if (!binding.isConstructor()) {
            //complex and multiple occurrence, declare as var
            if (!(scope instanceof Name) && varCnt > 1) {
                scopeVar = "tmp_";
                result.add(String.format("let %s = %s;", scopeVar, toStr(scope, cc)));
            }
            else {
                scopeVar = toStr(scope, cc);
            }
        }
        for (var line : info.target) {
            for (int i = 0; i < args.size(); i++) {
                line = line.replace("$" + (i + 1), toStr(args.get(i), cc));
            }
            line = line.replace("$0", scopeVar);
            result.add(line);
        }
        if (info.warning != null) {
            Logger.log(cc, info.warning);
        }
        return result;
    }

    static class JavaType {
        String name;
        boolean isGeneric;
        List<String> typeVars = new ArrayList<>();
        int dims;

        public JavaType(String name) {
            this.name = name;
        }

        public static JavaType parse(String name, ClassInfo classInfo) {
            if (classInfo.typeVars.contains(name)) {
                var res = new JavaType(name);
                res.isGeneric = true;
                return res;
            }
            int pos = name.indexOf("<");
            JavaType res;
            int off;
            if (pos != -1) {
                res = new JavaType(name.substring(0, pos));
                int end = name.indexOf(">");
                res.typeVars.addAll(Arrays.asList(name.substring(pos + 1, end).split(",")));
            }
            else {
                off = name.indexOf("[");
                if (off != -1) {
                    res = new JavaType(name.substring(0, off));
                }
                else {
                    res = new JavaType(name);
                }
            }
            res.dims(name);
            return res;
        }

        void dims(String name) {
            int off = name.indexOf("[");
            if (off != -1) {
                while (off < name.length() && name.indexOf("[]", off) != -1) {
                    off += 2;
                    dims++;
                }
            }
        }
    }

    MethodInfo findMethod(ClassInfo classInfo, IMethodBinding binding) {
        IMethodBinding real = binding.getMethodDeclaration();

        for (MethodInfo info : classInfo.methods) {
            if (!info.name.equals(real.getName())) continue;
            if (info.args.size() != binding.getParameterTypes().length) continue;
            boolean found = true;
            for (int i = 0; i < real.getParameterTypes().length; i++) {
                var t1 = JavaType.parse(info.args.get(i), classInfo);
                ITypeBinding t3 = real.getParameterTypes()[i];
                if (!isSame(t1, t3)) {
                    found = false;
                    break;
                }
            }
            if (found) {
                return info;
            }
        }
        return null;
    }

    boolean isSame(JavaType t1, ITypeBinding t2) {
        if (t1.isGeneric) return true;
        if (t2.isPrimitive()) {
            return t1.name.equals(t2.getName());
        }
        if (t1.dims != t2.getDimensions()) return false;
        if (t1.dims > 0) {
            return t1.name.equals(t2.getElementType().getQualifiedName());
        }
        return t1.name.equals(t2.getBinaryName());
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

    public ClassInfo findClass(ITypeBinding type) {
        return classMap.get(type.getBinaryName());
    }

    public CType mapType(ITypeBinding type) {
        if (classMap.containsKey(type.getBinaryName())) {
            ClassInfo info = classMap.get(type.getBinaryName());
            CType target = info.target.copy();
            target.typeNames.clear();
            //substitute type args
            for (int i = 0; i < info.typeVars.size(); i++) {
                target.typeNames.add(new CType(type.getTypeArguments()[i].getName()));
            }
            target.mapped = true;
            return target;
        }
        return new CType(type.getName());
    }

    public String mapParamName(String name) {
        if (Util.isKeyword(name)) {
            return map(name);
        }
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

    public CName mapFieldName(String name, ITypeBinding cc) {
        String org = name;
        CName res;
        if (Util.isKeyword(name)) {
            name = map(name);
        }
        else {
            for (IMethodBinding method : cc.getDeclaredMethods()) {
                if (method.getName().equals(name)) {
                    name = map(name);
                    break;
                }
            }
        }
        res = new CName(name);
        res.orgName = org;
        return res;
    }

    public static class ClassInfo {
        List<String> typeVars = new ArrayList<>();
        CType target;
        public List<String> includes = new ArrayList<>();
        List<MethodInfo> methods = new ArrayList<>();
    }

    public static class MethodInfo {
        String name;
        List<String> target = new ArrayList<>();
        String warning;
        List<String> args = new ArrayList<>();
        public List<String> includes = new ArrayList<>();
    }

    static class FieldInfo {
        String name;
        boolean external = false;
        String target;
    }
}
