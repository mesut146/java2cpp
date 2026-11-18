package com.mesut.j2cpp.visitor;

import com.mesut.j2cpp.Config;
import com.mesut.j2cpp.IncludeStmt;
import com.mesut.j2cpp.ast.*;
import com.mesut.j2cpp.cppast.expr.CInfixExpression;
import com.mesut.j2cpp.cppast.expr.CMethodInvocation;
import com.mesut.j2cpp.cppast.stmt.CBlockStatement;
import com.mesut.j2cpp.cppast.stmt.CExpressionStatement;
import com.mesut.j2cpp.cppast.stmt.CReturnStatement;
import com.mesut.j2cpp.map.ClassMap;
import com.mesut.j2cpp.map.Mapper;
import com.mesut.j2cpp.util.TypeHelper;
import org.eclipse.jdt.core.dom.RecordDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings("unchecked")
public class HeaderWriter {

    public List<CClass> classes = new ArrayList<>();
    public Namespace ns;
    SourceVisitor sourceVisitor;
    Code code;
    CompilationUnit cu;
    Path dir;
    List<AbstractTypeDeclaration> types = new ArrayList<>();

    public HeaderWriter(Path dir) {
        this.dir = dir;
    }

    public void all(CompilationUnit cu) {
        this.cu = cu;
        for (Object o : cu.types()) {
            AbstractTypeDeclaration decl = (AbstractTypeDeclaration) o;
            collect(decl);
        }
        for (AbstractTypeDeclaration decl : types) {
            visit(decl);
        }
    }

    void collect(AbstractTypeDeclaration decl) {
        types.add(decl);
        for (Object o : decl.bodyDeclarations()) {
            if (o instanceof AbstractTypeDeclaration) {//inner class
                collect((AbstractTypeDeclaration) o);
            }
        }
    }

    String cname(ITypeBinding binding) {
        String name = binding.getBinaryName()
                .replace("$", "_");
        int i = name.lastIndexOf(".");
        //if(i==-1) i=0;
        return name.substring(i + 1);
    }

    Path headerPath(ITypeBinding binding) {
        String name = binding.getBinaryName()
                .replace("$", "_")
                .replace(".", "/") +
                ".h";
        return Paths.get(dir.toString(), name);
    }

    public void visit(AbstractTypeDeclaration decl) {
        //includes
        if (decl instanceof EnumDeclaration) {
            visit((EnumDeclaration) decl);
        }
        else if (decl instanceof AnnotationTypeDeclaration) {
            //visit((AnnotationTypeDeclaration) decl, null);
        }
        else if (decl instanceof RecordDeclaration) {
            visit((RecordDeclaration) decl);
        }
        else {
            visit((TypeDeclaration) decl);
        }
    }

    void nsBegin() {
        PackageDeclaration pd = cu.getPackage();
        if (pd == null) return;
        String s = pd.getName().toString();
        //types in the current namespace should be printed without their prefix
        code.usings.add(s.replace(".", "::"));
        if (Config.ns_type_nested) {
            for (String cur : s.split("\\.")) {
                if (Config.ns_nested_indent) {
                    code.line("namespace %s{\n", cur);
                    code.up();
                }
                else {
                    code.write("namespace %s{\n", cur);
                }
            }
            code.write("\n");
        }
        else {
            code.line("namespace %s{\n", s.replace(".", "::"));
            if (Config.ns_indent) {
                code.up();
            }
        }
    }

    void nsEnd() {
        PackageDeclaration pd = cu.getPackage();
        if (pd == null) return;
        String s = pd.getName().toString();
        if (Config.ns_type_nested) {
            for (String cur : s.split("\\.")) {
                if (Config.ns_nested_indent) {
                    code.down();
                    code.line("\n}\n");
                }
                else {
                    code.write("\n}\n");
                }
            }
        }
        else {
            code.down();
            code.line("\n\n}");
        }
    }

    private void addSourceInclude(ITypeBinding binding, Set<String> headers) {
        if (binding == null) return;
        if (binding.isArray()) {
            addSourceInclude(binding.getElementType(), headers);
            return;
        }
        if (binding.isPrimitive() || binding.isTypeVariable() || binding.isWildcardType()) return;
        if (binding.isParameterizedType()) {
            for (ITypeBinding arg : binding.getTypeArguments()) addSourceInclude(arg, headers);
        }
        if (binding.isFromSource() && binding.getBinaryName() != null) {
            headers.add(binding.getBinaryName().replace("$", "_").replace(".", "/") + ".h");
        }
    }

    private Set<String> sourceIncludes(ITypeBinding binding, List<BodyDeclaration> bodies) {
        Set<String> headers = new LinkedHashSet<>();
        addSourceInclude(binding.getSuperclass(), headers);
        for (ITypeBinding iface : binding.getInterfaces()) addSourceInclude(iface, headers);
        for (BodyDeclaration body : bodies) {
            if (body instanceof FieldDeclaration) {
                for (VariableDeclarationFragment frag : (List<VariableDeclarationFragment>) ((FieldDeclaration) body).fragments()) {
                    addSourceInclude(frag.resolveBinding().getType(), headers);
                }
            }
            else if (body instanceof MethodDeclaration) {
                MethodDeclaration method = (MethodDeclaration) body;
                addSourceInclude(method.resolveBinding().getReturnType(), headers);
                for (Object param : method.parameters()) {
                    addSourceInclude(((SingleVariableDeclaration) param).getType().resolveBinding(), headers);
                }
            }
        }
        headers.remove(headerPath(binding).getFileName().toString());
        headers.remove(cname(binding) + ".h");
        return headers;
    }

    public void visit(TypeDeclaration node) {
        code = new Code();
        code.write("#pragma once\n\n#include <string>\n#include <vector>\n");

        ITypeBinding binding = node.resolveBinding();
        for (String header : sourceIncludes(binding, (List<BodyDeclaration>) (List<?>) node.bodyDeclarations())) {
            code.write("#include \"%s\"\n", header);
        }
        //mapped base types (e.g. Exception -> CppHelper::Throwable) need their headers too
        Set<String> mappedIncludes = new LinkedHashSet<>();
        if (binding.getSuperclass() != null && binding.getSuperclass().getBinaryName() != null) {
            Mapper.instance.collectIncludes(binding.getSuperclass().getBinaryName(), mappedIncludes);
        }
        for (ITypeBinding iface : binding.getInterfaces()) {
            if (iface.getBinaryName() != null) {
                Mapper.instance.collectIncludes(iface.getBinaryName(), mappedIncludes);
            }
        }
        for (String inc : mappedIncludes) {
            code.write("%s\n", new IncludeStmt(inc));
        }
        code.write("\n");

        nsBegin();

        ITypeBinding declaring = binding.getDeclaringClass();
        if (declaring != null && declaring.isFromSource()
                && !Modifier.isStatic(binding.getModifiers())) {
            //forward declaration for the enclosing instance pointer
            code.line("class %s;\n", cname(declaring));
        }

        ITypeBinding[] params = binding.getTypeParameters();
        if (params.length > 0) {
            StringBuilder template = new StringBuilder("template <");
            for (int i = 0; i < params.length; i++) {
                if (i > 0) template.append(", ");
                template.append("class ").append(params[i].getName());
            }
            code.line("%s>\n", template);
        }
        code.line("class %s: ", cname(binding));
        if (node.getSuperclassType() != null) {
            code.write("public %s", binding.getSuperclass());
        }
        else {
            code.write("public %s", TypeHelper.getObjectType());
        }
        for (ITypeBinding iface : binding.getInterfaces()) {
            code.write(", ");
            code.write("public %s", iface);
        }
        code.write("{\n");
        code.line("public:\n");
        //a polymorphic base is required for dynamic_cast on converted casts
        code.line("virtual ~%s() = default;\n", cname(binding));
        //non-static inner classes need a reference to the enclosing instance
        ITypeBinding outer = binding.getDeclaringClass();
        if (outer != null && outer.isFromSource() && !Modifier.isStatic(binding.getModifiers())) {
            code.line("%s* %s;\n", cname(outer), Config.parentName);
            code.line("%s(%s* %s) : %s(%s) {}\n",
                    cname(binding), cname(outer), Config.parentName,
                    Config.parentName, Config.parentName);
        }
        code.up();
        node.bodyDeclarations().forEach(body -> visitBody((BodyDeclaration) body, binding));
        //new DepVisitor(cc, node).handle();
        code.down();
        code.line("};");

        nsEnd();

        Path file = headerPath(binding);
        System.out.println("writing " + file);
        try {
            Files.createDirectories(file.getParent());
            Files.write(file, code.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        code = null;
    }

    //records are simple value types: emit fields for the components, a
    //canonical constructor and accessor methods
    public void visit(RecordDeclaration node) {
        code = new Code();
        code.write("#pragma once\n\n#include <string>\n#include <vector>\n#include \"CppHelper.h\"\n");
        ITypeBinding binding = node.resolveBinding();
        nsBegin();
        code.line("class %s: public %s", cname(binding), TypeHelper.getObjectType());
        for (ITypeBinding iface : binding.getInterfaces()) {
            code.write(", public %s", iface);
        }
        code.write("{\n");
        code.line("public:\n");
        code.line("virtual ~%s() = default;\n", cname(binding));
        //components become fields (prefixed to avoid colliding with accessors)
        for (Object o : node.recordComponents()) {
            SingleVariableDeclaration comp = (SingleVariableDeclaration) o;
            code.line("%s _%s;\n", code.ptr(comp.getType().resolveBinding()), comp.getName());
        }
        //canonical constructor
        code.write("%s(", cname(binding));
        for (int i = 0; i < node.recordComponents().size(); i++) {
            if (i > 0) code.write(", ");
            SingleVariableDeclaration comp = (SingleVariableDeclaration) node.recordComponents().get(i);
            code.write("%s p%s", code.ptr(comp.getType().resolveBinding()), i);
        }
        code.write(")");
        if (!node.recordComponents().isEmpty()) {
            code.write(" : ");
            for (int i = 0; i < node.recordComponents().size(); i++) {
                if (i > 0) code.write(", ");
                SingleVariableDeclaration comp = (SingleVariableDeclaration) node.recordComponents().get(i);
                code.write("_%s(p%d)", comp.getName(), i);
            }
        }
        code.write(" {}\n");
        //accessors: componentName()
        for (Object o : node.recordComponents()) {
            SingleVariableDeclaration comp = (SingleVariableDeclaration) o;
            code.write("%s %s() { return _%s; }\n", code.ptr(comp.getType().resolveBinding()),
                    comp.getName(), comp.getName());
        }
        //synthesized Object methods (toString/equals/hashCode)
        emitRecordObjectMethods(node, binding);
        //explicit methods declared in the record body
        for (Object o : node.bodyDeclarations()) {
            if (o instanceof MethodDeclaration) {
                visitBody((BodyDeclaration) o, binding);
            }
        }
        code.line("};");
        nsEnd();
        Path file = headerPath(binding);
        System.out.println("writing " + file);
        try {
            Files.createDirectories(file.getParent());
            Files.write(file, code.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        code = null;
    }

    //emit the three methods Java auto-generates for a record
    private void emitRecordObjectMethods(RecordDeclaration node, ITypeBinding binding) {
        List<SingleVariableDeclaration> comps = new ArrayList<>();
        for (Object o : node.recordComponents()) comps.add((SingleVariableDeclaration) o);
        String cls = cname(binding);

        //toString: Class[field=value, ...]
        code.write("std::string* toString() {\n");
        StringBuilder fmt = new StringBuilder(cls).append("[");
        for (int i = 0; i < comps.size(); i++) {
            if (i > 0) fmt.append(", ");
            fmt.append(comps.get(i).getName()).append("=%s");
        }
        fmt.append("]");
        code.write("return CppHelper::format(new std::string(\"%s\")", fmt);
        for (SingleVariableDeclaration c : comps) {
            code.write(", CppHelper::valueToString(_%s)", c.getName());
        }
        code.write(");\n}\n");

        //equals: same type and all components equal
        code.write("bool equals(java::lang::Object* o) {\n");
        if (comps.isEmpty()) {
            code.write("return dynamic_cast<%s*>(o) != nullptr;\n}\n", cls);
        }
        else {
            code.write("auto other = dynamic_cast<%s*>(o);\n", cls);
            code.write("if (!other) return false;\n");
            code.write("return ");
            for (int i = 0; i < comps.size(); i++) {
                if (i > 0) code.write(" && ");
                code.write("CppHelper::eq(_%s, other->_%s)", comps.get(i).getName(), comps.get(i).getName());
            }
            code.write(";\n}\n");
        }

        //hashCode: Java-style 31*h + component hash
        code.write("int hashCode() {\n");
        if (comps.isEmpty()) {
            code.write("return 1;\n}\n");
        }
        else {
            code.write("return CppHelper::hashCombine(");
            for (int i = 0; i < comps.size(); i++) {
                if (i > 0) code.write(", ");
                code.write("_%s", comps.get(i).getName());
            }
            code.write(");\n}\n");
        }
    }

    private void visitBody(BodyDeclaration body, ITypeBinding cc) {
        if (body instanceof FieldDeclaration) {
            visit((FieldDeclaration) body, cc);
        }
        else if (body instanceof MethodDeclaration) {
            visit((MethodDeclaration) body);
        }
        else if (body instanceof Initializer) {
            if (Modifier.isStatic(body.getModifiers())) {
                code.line("static void si();\n");
            }
        }
    }

    CBlockStatement initEnum(CClass cc) {
        //add ordinal field
        CField ord = new CField();
        ord.type = new CType("int");
        ord.name = new CName("ordinal");
        cc.addField(ord);
        //enum cons name field
        CField name = new CField();
        name.type = Mapper.instance.mapType(TypeHelper.getStringType(), cc);
        name.name = new CName("cons_name");
        cc.addField(name);
        //init method for ordinal & cons name
        CMethod init = new CMethod();
        init.name = new CName("init_cons");
        init.type = TypeHelper.getVoidType();
        init.setStatic(true);
        init.body = new CBlockStatement();
        cc.addMethod(init);
        //static init
        mainEntrySiCall(init);
        //toString method
        CMethod toStr = new CMethod();
        toStr.name = new CName("toString");
        toStr.type = Mapper.instance.mapType(TypeHelper.getStringType(), cc);
        toStr.body = new CBlockStatement();
        toStr.body.addStatement(new CReturnStatement(name.name));
        cc.addMethod(toStr);
        //compareTo method
        CMethod cmp = new CMethod();
        cmp.name = new CName("compareTo");
        cmp.type = new CType("int");
        cmp.addParam(new CParameter(cc.getType(), CName.simple("other")));
        cmp.body = new CBlockStatement();
        cmp.body.addStatement(new CReturnStatement(new CInfixExpression(CName.simple("ordinal"), new CMethodInvocation(CName.simple("other"), CName.simple("ordinal"), true), "==")));
        cc.addMethod(cmp);
        return init.body;
    }

    public void visit(EnumDeclaration n) {
        code = new Code();
        code.write("#pragma once\n\n#include <string>\n");

        ITypeBinding binding = n.resolveBinding();
        nsBegin();
        //Object has a generated stub; java::lang::Enum does not
        code.line("class %s: public %s{\n", cname(binding), TypeHelper.getObjectType());
        code.line("public:\n");
        code.up();
        int ordinal = 0;

        for (EnumConstantDeclaration cons : (List<EnumConstantDeclaration>) n.enumConstants()) {
            code.line("static %s %s;\n", code.ptr(binding), cons.getName());
            ordinal++;
        }

        //per-constant data and the standard Enum accessors, unless the source declares them
        Set<String> declared = new java.util.HashSet<>();
        for (Object o : n.bodyDeclarations()) {
            if (o instanceof MethodDeclaration) {
                declared.add(((MethodDeclaration) o).getName().getIdentifier());
            }
            else if (o instanceof FieldDeclaration) {
                for (Object frag : ((FieldDeclaration) o).fragments()) {
                    declared.add(((VariableDeclarationFragment) frag).getName().getIdentifier());
                }
            }
            else if (o instanceof EnumConstantDeclaration) {
                declared.add(((EnumConstantDeclaration) o).getName().getIdentifier());
            }
        }
        String cls = cname(binding);
        CType str = Mapper.instance.mapType(TypeHelper.getStringType(), null);
        CType strPtr = str.copy();
        strPtr.setPointer(Config.ptr_method);
        code.line("int _ordinal;\n");
        code.line("%s _name;\n", strPtr);
        if (!declared.contains("ordinal")) {
            code.line("int ordinal() { return _ordinal; }\n");
        }
        if (!declared.contains("name")) {
            code.line("%s name() { return _name; }\n", strPtr);
        }
        if (!declared.contains("toString")) {
            code.line("%s toString() { return _name; }\n", strPtr);
        }
        if (!declared.contains("compareTo")) {
            code.line("int compareTo(%s* other) { return _ordinal - other->_ordinal; }\n", cls);
        }

        if (!n.bodyDeclarations().isEmpty()) {
            n.bodyDeclarations().forEach(p -> visitBody((BodyDeclaration) p, binding));
        }
        code.down();
        code.line("};");
        nsEnd();
        Path file = headerPath(binding);
        System.out.println("writing " + file);
        try {
            Files.createDirectories(file.getParent());
            Files.write(file, code.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        code = null;
    }

    public void visit(FieldDeclaration node, ITypeBinding clazz) {
        boolean isStatic = Modifier.isStatic(node.getModifiers()) || clazz.isInterface();
        for (VariableDeclarationFragment frag : (List<VariableDeclarationFragment>) node.fragments()) {
            ITypeBinding type = frag.resolveBinding().getType();
            boolean coe = isStatic && frag.getInitializer() != null && type.isPrimitive() && Modifier.isFinal(node.getModifiers());
            boolean cof = isStatic && Config.static_field_cofui && Modifier.isFinal(node.getModifiers()) && !type.isPrimitive();

            code.line("");
            if (isStatic) {
                code.write("static ");
                if (coe) {
                    code.write("constexpr ");
                }
            }
            code.write("%s %s", code.ptr(type), Mapper.instance.mapFieldName(frag.getName().getIdentifier(), clazz));
            if (cof) {
                code.write("();\n");
                return;
            }
            if (coe || frag.getInitializer() != null && !Config.fields_in_constructors) {
                sourceVisitor = new SourceVisitor(null, null);
                sourceVisitor.binding = clazz;
                frag.getInitializer().accept(sourceVisitor);
                code.write(" = %s;\n", sourceVisitor.code.toString());
                sourceVisitor = null;
            }
            else {
                code.write(";\n");
            }
        }
    }

    public void visit(MethodDeclaration node) {
        IMethodBinding binding = node.resolveBinding();
        List<ITypeBinding> methodParams = TypeHelper.methodTypeParams(binding, binding.getDeclaringClass());
        TypeVisitor.Wildcards wildcards = new TypeVisitor.Wildcards();
        for (Object o : node.parameters()) {
            TypeVisitor.collectWildcards(((SingleVariableDeclaration) o).getType().resolveBinding(), wildcards);
        }
        boolean oldKeep = TypeVisitor.keepTypeVars;
        Set<String> oldVars = TypeVisitor.activeTypeVars;
        TypeVisitor.activeTypeVars = new java.util.HashSet<>();
        TypeVisitor.keepTypeVars = !methodParams.isEmpty() && Config.useMappers;
        for (ITypeBinding p : methodParams) {
            TypeVisitor.activeTypeVars.add(p.getName());
        }
        List<String> templateNames = new ArrayList<>();
        for (ITypeBinding p : methodParams) templateNames.add(p.getName());
        templateNames.addAll(wildcards.names);
        String methodTemplate = TypeHelper.templateLineForNames(templateNames);
        if (methodTemplate != null) {
            String note = wildcards.isEmpty()
                    ? "method type parameters kept as C++ template: " + methodTemplate
                    : "wildcard type arguments templated: " + wildcards.describe();
            code.noteLine(note);
            code.write("%s\n", methodTemplate);
        }
        else {
            String erasure = TypeHelper.erasureNote(binding);
            if (erasure != null) {
                code.noteLine(erasure);
            }
        }
        if (binding.isConstructor()) {
            code.line("");
        }
        else if (Modifier.isStatic(node.getModifiers())) {
            code.line("static ");
        }
        else {
            code.line("");
        }
        boolean abstractMethod = binding.getDeclaringClass().isInterface() && !binding.isConstructor();
        CMethod cm = ClassMap.sourceMap.getMethod(binding);
        boolean virtualMethod = !Modifier.isStatic(binding.getModifiers())
                && cm != null && cm.isVirtual();
        if (abstractMethod || virtualMethod) {
            code.write("virtual ");
        }
        if (!binding.isConstructor()) {
            code.write("%s ", code.ptr(binding.getReturnType()));
        }
        String methodName = binding.isConstructor()
                ? cname(binding.getDeclaringClass())
                : Mapper.instance.mapMethodName(node.getName().getIdentifier());
        code.write("%s(", methodName);
        for (int i = 0; i < node.parameters().size(); i++) {
            SingleVariableDeclaration param = (SingleVariableDeclaration) node.parameters().get(i);

            ITypeBinding ptype = param.getType().resolveBinding();
            String rendered = wildcards.isEmpty() ? code.ptr(ptype)
                    : code.ptrSmart(TypeVisitor.fromBinding(ptype, null, wildcards));
            code.write("%s %s", rendered, Mapper.instance.mapParamName(param.getName().getIdentifier()));
            if (i < node.parameters().size() - 1) code.write(", ");
        }
        code.write(")");
        if (cm != null && cm.isOverride && !Modifier.isStatic(binding.getModifiers())) {
            code.write(" override");
        }
        if (abstractMethod) {
            code.write(" = 0;\n");
        }
        else {
            code.write(";\n");
        }
        TypeVisitor.keepTypeVars = oldKeep;
        TypeVisitor.activeTypeVars = oldVars;
    }

    //make main entry call this method
    void mainEntrySiCall(CMethod method) {
        //main entry should call this
        CMethod si = getSiInit();
        CMethodInvocation call = new CMethodInvocation(method.parent.getType(), method.name, false);
        si.body.addStatement(new CExpressionStatement(call));
    }

    //static initializer
    public void visit(Initializer node, ITypeBinding cc) {
        CMethod method = new CMethod();
        method.name = CName.from(Config.static_init_name);
        method.type = TypeHelper.getVoidType();
        method.setStatic(true);


        //main entry should call this
        mainEntrySiCall(method);
    }

    //get static initializer method init main entry
    CMethod getSiInit() {
        for (CMethod method : ClassMap.sourceMap.mainClass.methods) {
            if (method.name.is("si_init")) {
                return method;
            }
        }
        return null;//never happens
    }
}
