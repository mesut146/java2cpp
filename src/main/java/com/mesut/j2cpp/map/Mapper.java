package com.mesut.j2cpp.map;

import com.mesut.j2cpp.Config;
import com.mesut.j2cpp.IncludeStmt;
import com.mesut.j2cpp.Logger;
import com.mesut.j2cpp.Util;
import com.mesut.j2cpp.ast.CClass;
import com.mesut.j2cpp.ast.CMethod;
import com.mesut.j2cpp.ast.CName;
import com.mesut.j2cpp.ast.CType;
import com.mesut.j2cpp.visitor.SourceVisitor;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
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
        String[] all = {"list.json", "map.json", "set.json", "string.json", "stack.json",
                "Boolean.json", "Integer.json", "sys.json", "arrays.json", "math.json",
                "primitives.json", "double.json", "float.json", "long.json", "short.json",
                "stringbuilder.json", "objects.json", "optional.json", "random.json", "uuid.json",
                "printstream.json", "file.json", "path.json", "files.json",
                "streams.json", "bufferedreader.json", "scanner.json", "printwriter.json",
                "throwable.json", "number.json"};
        for (String mapper : all) {
            addMapper(getClass().getResourceAsStream("/cpp/" + mapper));
        }
    }

    public void addMapper(String jsonPath) throws IOException {
        addMapper(new FileInputStream(jsonPath));
    }

    //names may be comma separated and may contain generics like Map<K,V>
    static List<String> splitNames(String names) {
        List<String> result = new ArrayList<>();
        int depth = 0;
        int start = 0;
        for (int i = 0; i < names.length(); i++) {
            char c = names.charAt(i);
            if (c == '<') depth++;
            else if (c == '>') depth--;
            else if (c == ',' && depth == 0) {
                result.add(names.substring(start, i).trim());
                start = i + 1;
            }
        }
        result.add(names.substring(start).trim());
        return result;
    }

    public void collectIncludes(String binaryName, Set<String> out) {
        if (!Config.useMappers) return;
        ClassInfo info = classMap.get(binaryName);
        if (info != null) out.addAll(info.includes);
    }

    public void addMapper(InputStream is) throws IOException {
        JSONObject cls = new JSONObject(Util.read(is));
        String target = cls.optString("target", null);

        ClassInfo info = new ClassInfo();
        if (target != null) {
            info.target = CType.parse(target);
        }
        JSONObject fieldValues = cls.optJSONObject("fields");
        if (fieldValues != null) {
            for (String key : fieldValues.keySet()) {
                info.fields.put(key, fieldValues.getString(key));
            }
        }

        for (String name : splitNames(cls.getString("name"))) {
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

        JSONArray methods = cls.getJSONArray("methods");
        for (int i = 0; i < methods.length(); i++) {
            JSONObject method = methods.getJSONObject(i);
            MethodInfo methodInfo = new MethodInfo();
            methodInfo.owner = info;
            parseSignature(methodInfo, method.getString("name"));
            methodInfo.external = method.optBoolean("external", false);
            methodInfo.expr = method.optString("expr", null);
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
        return toStr(expr, binding, null);
    }

    //current visitor, used to render mapped expressions so it sees context such
    //as captured locals in anonymous classes
    public Object renderer;

    String toStr(Expression expr, ITypeBinding binding, Set<String> out) {
        if (expr == null) return "";
        if (renderer instanceof SourceVisitor) {
            return ((SourceVisitor) renderer).renderExpr(expr, binding, out);
        }
        return SourceVisitor.render(expr, binding, out);
    }

    public MethodInfo findInfo(IMethodBinding binding) {
        return findInfo(binding, binding.getParameterTypes().length);
    }

    public MethodInfo findInfo(IMethodBinding binding, int argCount) {
        if (!Config.useMappers) return null;
        ClassInfo classInfo = classMap.get(binding.getDeclaringClass().getBinaryName());
        if (classInfo == null) return null;//no mapping for this type
        return findMethod(classInfo, binding, argCount);
    }

    //a method can be declared on a supertype of the mapped class (e.g. Set.add)
    public MethodInfo findInfo(IMethodBinding binding, ITypeBinding receiver) {
        return findInfo(binding, receiver, binding.getParameterTypes().length);
    }

    public MethodInfo findInfo(IMethodBinding binding, ITypeBinding receiver, int argCount) {
        if (!Config.useMappers) return null;
        MethodInfo info = findInfo(binding, argCount);
        if (info != null || receiver == null) return info;
        return findInfoInHierarchy(binding, receiver, new HashSet<>(), argCount);
    }

    private MethodInfo findInfoInHierarchy(IMethodBinding binding, ITypeBinding type, Set<ITypeBinding> seen, int argCount) {
        if (type == null || !seen.add(type)) return null;
        ClassInfo classInfo = classMap.get(type.getBinaryName());
        if (classInfo != null) {
            MethodInfo info = findMethod(classInfo, binding, argCount);
            if (info != null) return info;
        }
        MethodInfo info = findInfoInHierarchy(binding, type.getSuperclass(), seen, argCount);
        if (info != null) return info;
        for (ITypeBinding iface : type.getInterfaces()) {
            info = findInfoInHierarchy(binding, iface, seen, argCount);
            if (info != null) return info;
        }
        return null;
    }

    public List<String> mapMethod(IMethodBinding binding, List<Expression> args, Expression scope, ITypeBinding cc) {
        return mapMethod(findInfo(binding, args.size()), binding, args, scope, cc, null);
    }

    public List<String> mapMethod(IMethodBinding binding, List<Expression> args, Expression scope, ITypeBinding cc, ITypeBinding receiver) {
        return mapMethod(findInfo(binding, receiver, args.size()), binding, args, scope, cc, null);
    }

    public List<String> mapMethod(IMethodBinding binding, List<Expression> args, Expression scope, ITypeBinding cc, ITypeBinding receiver, Set<String> out) {
        return mapMethod(findInfo(binding, receiver, args.size()), binding, args, scope, cc, out);
    }

    private List<String> mapMethod(MethodInfo info, IMethodBinding binding, List<Expression> args, Expression scope, ITypeBinding cc, Set<String> out) {
        if (info == null) {
            if (Config.useMappers) {
                Logger.log(String.format("missing mapper in '%s' method='%s'", cc, binding));
            }
            return null;
        }
        if (out != null) {
            if (info.owner != null) out.addAll(info.owner.includes);
            out.addAll(info.includes);
        }
        //count var appearance
        List<String> result = new ArrayList<>();
        int varCnt = 0;
        for (var line : info.target) {
            var m = Pattern.compile(VAR_PATTERN).matcher(line);
            while (m.find()) {
                varCnt++;
            }
        }
        String scopeVar = "";
        if (!binding.isConstructor() && scope != null) {
            //complex and multiple occurrence, declare as var
            if (!(scope instanceof Name) && varCnt > 1) {
                scopeVar = "tmp_";
                result.add(String.format("auto %s = %s;", scopeVar, toStr(scope, cc, out)));
            }
            else {
                scopeVar = toStr(scope, cc, out);
            }
        }
        if (info.expr != null) {
            for (var line : info.target) {
                result.add(substitute(line, args, cc, scopeVar, out));
            }
            String expr = substitute(info.expr, args, cc, scopeVar, out);
            result.add(prefixScope(info, info.expr, expr, scopeVar));
        }
        else {
            for (int i = 0; i < info.target.size(); i++) {
                String line = info.target.get(i);
                String mapped = substitute(line, args, cc, scopeVar, out);
                if (i == info.target.size() - 1) {
                    mapped = prefixScope(info, line, mapped, scopeVar);
                }
                result.add(mapped);
            }
        }
        if (info.warning != null) {
            Logger.log(cc, info.warning);
        }
        return result;
    }

    private static final String VAR_PATTERN = "\\$\\{varName\\}|\\$0";

    private String substitute(String line, List<Expression> args, ITypeBinding cc, String scopeVar, Set<String> out) {
        for (int i = 0; i < args.size(); i++) {
            line = line.replace("$" + (i + 1), toStr(args.get(i), cc, out));
        }
        line = line.replace("${varName}", scopeVar);
        return line.replace("$0", scopeVar);
    }

    //cpp targets without an explicit scope are member calls on the scope
    private static String prefixScope(MethodInfo info, String original, String mapped, String scopeVar) {
        if (info.external || info.expr != null || info.target.size() > 1 || scopeVar.isEmpty()) return mapped;
        if (Pattern.compile(VAR_PATTERN).matcher(original).find()) return mapped;
        return scopeVar + "->" + mapped;
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
            //mappers spell type variables as <T> or T
            String simple = name.startsWith("<") && name.indexOf(">") != -1
                    ? name.substring(1, name.indexOf(">")) : name;
            simple = Util.trimSuffix(simple, "[]");
            if (classInfo.typeVars.contains(simple)) {
                var res = new JavaType(simple);
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

    MethodInfo findMethod(ClassInfo classInfo, IMethodBinding binding, int argCount) {
        IMethodBinding real = binding.getMethodDeclaration();
        ITypeBinding[] params = real.getParameterTypes();

        for (MethodInfo info : classInfo.methods) {
            if (!info.name.equals(real.getName())) continue;
            if (info.args.size() != argCount) continue;
            boolean found = true;
            for (int i = 0; i < info.args.size(); i++) {
                var t1 = JavaType.parse(info.args.get(i), classInfo);
                ITypeBinding t3;
                if (binding.isVarargs() && i >= params.length - 1) {
                    //a mapper may spell the vararg as an array (Object[]) or as
                    //repeated element parameters (T,T,T)
                    t3 = t1.dims > 0 ? params[params.length - 1]
                            : params[params.length - 1].getElementType();
                }
                else {
                    t3 = params[i];
                }
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
        if (t1.isGeneric || t2.isTypeVariable()) return true;
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
        if (!Config.useMappers) return type;
        if (classMap.containsKey(type.realName)) {
            ClassInfo info = classMap.get(type.realName);
            if (info.target == null) return type;
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
        if (!Config.useMappers) return null;
        return classMap.get(type.getBinaryName());
    }

    public static int typeVarCount(ClassInfo info) {
        return info == null ? 0 : info.typeVars.size();
    }

    public CType mapType(ITypeBinding type) {
        if (!Config.useMappers) return new CType(type.getName());
        if (classMap.containsKey(type.getBinaryName())) {
            ClassInfo info = classMap.get(type.getBinaryName());
            if (info.target == null) return new CType(type.getName());
            CType target = info.target.copy();
            target.typeNames.clear();
            //substitute type args
            int count = Math.min(info.typeVars.size(), type.getTypeArguments().length);
            for (int i = 0; i < count; i++) {
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

    public String mapMethodName(String name) {
        return mapParamName(name);
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

    //constant fields that are initialized in a Java static initializer are plain
    //declarations in stubs; mappers may supply their literal value instead
    public String mapFieldValue(IVariableBinding binding) {
        if (!Config.useMappers || binding == null || binding.getDeclaringClass() == null) return null;
        ClassInfo info = classMap.get(binding.getDeclaringClass().getBinaryName());
        if (info == null) return null;
        return info.fields.get(binding.getName());
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
        public CType target;
        public List<String> includes = new ArrayList<>();
        List<MethodInfo> methods = new ArrayList<>();
        Map<String, String> fields = new HashMap<>();
    }

    public static class MethodInfo {
        String name;
        List<String> target = new ArrayList<>();
        String warning;
        List<String> args = new ArrayList<>();
        public List<String> includes = new ArrayList<>();
        public boolean external;
        public String expr;
        public ClassInfo owner;
    }
}
