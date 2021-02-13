package com.mesut.j2cpp.map;

import com.mesut.j2cpp.IncludeStmt;
import com.mesut.j2cpp.Util;
import com.mesut.j2cpp.ast.CMethod;
import com.mesut.j2cpp.ast.CSource;
import com.mesut.j2cpp.ast.CType;
import com.mesut.j2cpp.cppast.CExpression;
import com.mesut.j2cpp.visitor.TypeVisitor;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;

public class Mapper {

    public static Mapper instance = new Mapper();
    Map<String, ClassInfo> classMap;//java class -> holder

    public Mapper() {
        classMap = new HashMap<>();
    }

    static void parseSignature(MethodInfo methodInfo) {
        String sig = methodInfo.str;
        int parL = sig.indexOf("(");
        int parR = sig.lastIndexOf(")");
        String name = sig.substring(0, parL);
        String argStr = sig.substring(parL + 1, parR);

        String[] args = argStr.split(",");
        for (String type : args) {
            methodInfo.args.add(parseType(type));
        }

        methodInfo.name = name;
    }

    static CType parseType(String str) {
        if (str.startsWith("<")) {
            return new CType(str.substring(1, str.length() - 1), true);
        }
        return new CType(str);
    }

    static String subs(MethodInfo info) {
        return "";
    }

    public void addMapper(String jsonPath) throws IOException {
        JSONObject cls = new JSONObject(Util.read(new File(jsonPath)));
        List<CType> fromTypes = new ArrayList<>();
        String target = cls.getString("target");
        String include = cls.getString("include");

        ClassInfo info = new ClassInfo();
        info.target = new CType(target);
        for (String name : cls.getString("name").split(",")) {
            classMap.put(name, info);
        }

        JSONArray methods = cls.getJSONArray("methods");
        for (int i = 0; i < methods.length(); i++) {
            JSONObject method = methods.getJSONObject(i);
            MethodInfo methodInfo = new MethodInfo();
            methodInfo.str = method.getString("name");
            methodInfo.targetExpr = method.getString("target");
            methodInfo.external = method.optBoolean("external", false);
            info.methods.add(methodInfo);
            parseSignature(methodInfo);
        }
    }

    public CExpression mapMethod(IMethodBinding binding, List<CExpression> args) {
        CType type = TypeVisitor.fromBinding(binding.getDeclaringClass());
        ClassInfo classInfo = classMap.get(type.realName);
        if (classInfo == null) return null;//no mapping for this type
        List<CType> argTypes = new ArrayList<>();
        for (ITypeBinding at : binding.getParameterTypes()) {
            argTypes.add(TypeVisitor.fromBinding(at));
        }
        for (MethodInfo info : classInfo.methods) {
            if (!info.name.equals(binding.getName())) continue;
            boolean found = true;
            for (int i = 0; i < argTypes.size(); i++) {
                CType t1 = argTypes.get(i);
                CType t2 = info.args.get(i);
                if (t1.isTemplate) {
                    if (!t2.isTemplate) {
                        found = false;
                        break;
                    }
                }
                else {
                    if (!t1.equals(t2)) {
                        found = false;
                        break;
                    }
                }
            }
            if (found) {
                //do mapping

                break;
            }
        }
        return null;
    }

    /*MethodInfo findMethod(ClassInfo classInfo,){

    }*/

    public CType mapType(CType type, CSource source) {
        if (classMap.containsKey(type.realName)) {
            ClassInfo info = classMap.get(type.realName);
            if (info.includes != null) {
                if (source != null)
                    for (String inc : info.includes) {
                        source.addInclude(new IncludeStmt(inc));
                    }
            }
            CType target = info.target.copy();
            target.typeNames = type.typeNames;
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

    static class ClassInfo {
        CType target;
        List<String> includes;
        List<MethodInfo> methods = new ArrayList<>();
    }

    static class MethodInfo {
        String name;
        String str;
        String targetExpr;
        boolean external = false;
        List<CType> args = new ArrayList<>();
    }
}
