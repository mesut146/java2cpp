package com.mesut.j2cpp.visitor;

import com.mesut.j2cpp.Config;
import com.mesut.j2cpp.ast.*;
import com.mesut.j2cpp.cppast.expr.CInfixExpression;
import com.mesut.j2cpp.cppast.expr.CMethodInvocation;
import com.mesut.j2cpp.cppast.stmt.CBlockStatement;
import com.mesut.j2cpp.cppast.stmt.CExpressionStatement;
import com.mesut.j2cpp.cppast.stmt.CReturnStatement;
import com.mesut.j2cpp.map.ClassMap;
import com.mesut.j2cpp.map.Mapper;
import com.mesut.j2cpp.util.TypeHelper;
import org.eclipse.jdt.core.dom.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public class HeaderWriter {

    public List<CClass> classes = new ArrayList<>();
    public Namespace ns;
    SourceVisitor2 sourceVisitor2;
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
        else {
            visit((TypeDeclaration) decl);
        }
    }

    void nsBegin() {
        PackageDeclaration pd = cu.getPackage();
        if (pd == null) return;
        String s = pd.getName().toString();
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

    public void visit(TypeDeclaration node) {
        code = new Code();
        code.write("#pragma once\n\n");

        nsBegin();

        ITypeBinding binding = node.resolveBinding();
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

    private void visitBody(BodyDeclaration body, ITypeBinding cc) {
        if (body instanceof FieldDeclaration) {
            visit((FieldDeclaration) body, cc);
        }
        else if (body instanceof MethodDeclaration) {
            visit((MethodDeclaration) body);
        }
        else if (body instanceof Initializer) {
            //visit((Initializer) body, cc);
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
        code.write("#pragma once\n\n");

        code.line("class %s: public %s{\n", n.getName(), TypeHelper.getEnumType());
        code.line("public:\n");
        code.up();
        //CBlockStatement block = initEnum(cc);
        int ordinal = 0;
        ITypeBinding binding = n.resolveBinding();

        for (EnumConstantDeclaration cons : (List<EnumConstantDeclaration>) n.enumConstants()) {
            //constant.resolveVariable();
            code.line("static %s %s;\n", code.ptr(binding), cons.getName());
            if (!cons.arguments().isEmpty()) {
                //rhs.args = sourceVisitor.list(constant.arguments());
            }
            if (cons.getAnonymousClassDeclaration() != null) {
                //CClassInstanceCreation creation = new AnonyHandler().handle(constant.getAnonymousClassDeclaration(), cc.getType(), cc, sourceVisitor);
                //rhs.setType(creation.type);
            }
            ordinal++;
        }

        if (!n.bodyDeclarations().isEmpty()) {
            n.bodyDeclarations().forEach(p -> visitBody((BodyDeclaration) p, binding));
        }
        code.down();
        code.line("};");
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
            code.write("%s %s", code.ptr(type), frag.getName());
            if (cof) {
                code.write("();\n");
                return;
            }
            if (coe || frag.getInitializer() != null && !Config.fields_in_constructors) {
                sourceVisitor2 = new SourceVisitor2(null, null);
                sourceVisitor2.binding = clazz;
                frag.getInitializer().accept(sourceVisitor2);
                code.write(" = %s;\n", sourceVisitor2.code.toString());
                sourceVisitor2 = null;
            }
            else {
                code.write(";\n");
            }
        }
    }

    public void visit(MethodDeclaration node) {
        if (Modifier.isStatic(node.getModifiers())) code.line("static ");
        else code.line("");
        IMethodBinding binding = node.resolveBinding();
        code.write("%s %s(", code.ptr(binding.getReturnType()), node.getName());
        for (int i = 0; i < node.parameters().size(); i++) {
            SingleVariableDeclaration param = (SingleVariableDeclaration) node.parameters().get(i);

            code.write("%s %s", code.ptr(param.getType().resolveBinding()), param.getName().getIdentifier());
            if (i < node.parameters().size() - 1) code.write(", ");
        }
        code.write(");\n");
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
        //cc.addMethod(method);
        method.name = CName.from(Config.static_init_name);
        method.type = TypeHelper.getVoidType();
        method.setStatic(true);

        //sourceVisitor2.binding = cc;
        //method.body = (CBlockStatement) sourceVisitor.visitExpr(node.getBody(), null);

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
