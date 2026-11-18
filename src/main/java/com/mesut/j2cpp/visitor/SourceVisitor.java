package com.mesut.j2cpp.visitor;

import com.mesut.j2cpp.Config;
import com.mesut.j2cpp.IncludeStmt;
import com.mesut.j2cpp.LibHandler;
import com.mesut.j2cpp.Logger;
import com.mesut.j2cpp.ast.CName;
import com.mesut.j2cpp.ast.CType;
import com.mesut.j2cpp.cppast.CExpression;
import com.mesut.j2cpp.cppast.expr.CFieldAccess;
import com.mesut.j2cpp.cppast.expr.CMethodInvocation;
import com.mesut.j2cpp.map.Mapper;
import com.mesut.j2cpp.util.TypeHelper;
import org.eclipse.jdt.core.dom.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("unchecked")
public class SourceVisitor extends ASTVisitor {

    Path dir;
    CompilationUnit cu;
    ITypeBinding binding;
    IMethodBinding currentMethod;
    boolean tryLambdaVoidMethod;
    boolean emitFallbackReturn;
    Block currentMethodBody;
    String catchName;
    Code code = new Code();
    int forVarCnt = 0;
    int anonyCnt = 0;
    int resGuardCnt = 0;
    SwitchStatement switchNode;
    String switchEndLabel;
    boolean forInit;
    boolean throwDirect;
    StringBuilder anonyDecls = new StringBuilder();
    //captured locals for the anonymous class currently being rendered
    Map<IVariableBinding, String> anonCaptures = new HashMap<>();
    //local classes generated in this source, keyed by binary name
    Map<String, LocalClass> localClasses = new HashMap<>();
    //record component names currently being generated (emitted as _name fields)
    Set<String> recordComponents = new HashSet<>();
    //pattern variable names to leave as-is
    Map<String, String> patternCasts = new HashMap<>();

    static class LocalClass {
        final String name;
        final List<IVariableBinding> captures;
        final boolean needsParent;
        LocalClass(String name, List<IVariableBinding> captures, boolean needsParent) {
            this.name = name;
            this.captures = captures;
            this.needsParent = needsParent;
        }
    }
    //includes required by mapped methods, emitted before the generated body
    Set<String> includes = new LinkedHashSet<>();
    Set<String> selfHeaders = new LinkedHashSet<>();
    Set<ASTNode> preambleEmitted = new LinkedHashSet<>();
    Map<ASTNode, List<String>> mappedCalls = new HashMap<>();

    public SourceVisitor(Path dir, CompilationUnit cu) {
        this.dir = dir;
        this.cu = cu;
        Mapper.instance.renderer = this;
    }

    static String cnameOf(ITypeBinding type) {
        String binary = type.getBinaryName();
        if (binary == null) return type.getName();
        String name = binary.replace("$", "_");
        return name.substring(name.lastIndexOf('.') + 1);
    }

    //render a single expression as c++ without a surrounding compilation unit
    public static String render(Expression expr, ITypeBinding binding) {
        return render(expr, binding, null);
    }

    public static String render(Expression expr, ITypeBinding binding, Set<String> includes) {
        SourceVisitor visitor = new SourceVisitor(null, null);
        visitor.binding = binding;
        expr.accept(visitor);
        if (includes != null) includes.addAll(visitor.includes);
        return visitor.code.toString();
    }

    //instance render: reuses this visitor's state (e.g. anonCaptures) so mapped
    //method scopes/args resolve captured locals correctly
    public String renderExpr(Expression expr, ITypeBinding binding, Set<String> includes) {
        ITypeBinding savedBinding = this.binding;
        Code savedCode = this.code;
        this.binding = binding;
        this.code = new Code();
        expr.accept(this);
        if (includes != null) includes.addAll(this.includes);
        String result = this.code.toString();
        this.code = savedCode;
        this.binding = savedBinding;
        return result;
    }

    private List<String> mapped(MethodInvocation node) {
        if (!Config.useMappers) return null;
        IMethodBinding b = node.resolveMethodBinding();
        if (b == null) return null;
        if (mappedCalls.containsKey(node)) return mappedCalls.get(node);
        Expression scope = node.getExpression();
        ITypeBinding receiver = scope == null ? null : scope.resolveTypeBinding();
        //a type-qualified static call has no runtime receiver
        Expression mappedScope = isTypeExpression(scope) && Modifier.isStatic(b.getModifiers()) ? null : scope;
        List<String> res = Mapper.instance.mapMethod(b, node.arguments(), mappedScope, this.binding, receiver, includes);
        //a catch variable is a C++ reference (not a pointer); mapper prefixes use ->
        if (res != null && scope != null && catchName != null && catchName.equals(scope.toString())) {
            String recv = scope.toString();
            res = new ArrayList<>(res);
            for (int i = 0; i < res.size(); i++) {
                res.set(i, res.get(i).replace(recv + "->", recv + "."));
            }
        }
        mappedCalls.put(node, res);
        return res;
    }

    private void addLibInclude(ITypeBinding type) {
        if (type == null || !Config.writeLibHeader || type.isFromSource()) return;
        String name = type.getBinaryName();
        if (name == null) return;
        //a mapper without a target (e.g. System) still needs its stub header
        Mapper.ClassInfo mapped = Mapper.instance.findClass(type);
        if (mapped != null && mapped.target != null) return;
        String header = "lib/" + name.replace("$", "_").replace(".", "/") + ".h";
        if (includes.contains(header)) return;
        //make sure the stub and its bases are generated, and include bases first
        ITypeBinding parent = type.getSuperclass();
        if (parent != null) addLibInclude(parent);
        for (ITypeBinding iface : type.getInterfaces()) addLibInclude(iface);
        LibHandler.instance.addType(type);
        includes.add(header);
    }

    //collect locals referenced in the anonymous class body that are declared
    //outside it (Java effectively-final captures), in stable order
    private List<IVariableBinding> collectCaptures(AnonymousClassDeclaration anon) {
        Set<IVariableBinding> local = new HashSet<>();
        for (Object o : anon.bodyDeclarations()) {
            if (o instanceof MethodDeclaration) {
                for (Object p : ((MethodDeclaration) o).parameters()) {
                    IBinding b = ((SingleVariableDeclaration) p).resolveBinding();
                    if (b instanceof IVariableBinding) local.add((IVariableBinding) b);
                }
            }
        }
        anon.accept(new ASTVisitor() {
            @Override
            public boolean visit(VariableDeclarationFragment n) {
                if (n.resolveBinding() instanceof IVariableBinding) local.add((IVariableBinding) n.resolveBinding());
                return true;
            }
        });
        List<IVariableBinding> captures = new ArrayList<>();
        Set<IVariableBinding> seen = new HashSet<>();
        anon.accept(new ASTVisitor() {
            @Override
            public boolean visit(SimpleName n) {
                IBinding b = n.resolveBinding();
                if (b instanceof IVariableBinding && !((IVariableBinding) b).isField()
                        && !local.contains(b) && seen.add((IVariableBinding) b)) {
                    captures.add((IVariableBinding) b);
                }
                return true;
            }
        });
        return captures;
    }

    //capture detection and body rendering for a named local class (TypeDeclaration)
    private List<IVariableBinding> collectTypeCaptures(TypeDeclaration td) {
        Set<IVariableBinding> local = new HashSet<>();
        for (Object o : td.bodyDeclarations()) {
            if (o instanceof MethodDeclaration) {
                for (Object p : ((MethodDeclaration) o).parameters()) {
                    IBinding b = ((SingleVariableDeclaration) p).resolveBinding();
                    if (b instanceof IVariableBinding) local.add((IVariableBinding) b);
                }
            }
        }
        td.accept(new ASTVisitor() {
            @Override
            public boolean visit(VariableDeclarationFragment n) {
                if (n.resolveBinding() instanceof IVariableBinding) local.add((IVariableBinding) n.resolveBinding());
                return true;
            }
        });
        List<IVariableBinding> captures = new ArrayList<>();
        Set<IVariableBinding> seen = new HashSet<>();
        td.accept(new ASTVisitor() {
            @Override
            public boolean visit(SimpleName n) {
                IBinding b = n.resolveBinding();
                if (b instanceof IVariableBinding && !((IVariableBinding) b).isField()
                        && !local.contains(b) && seen.add((IVariableBinding) b)) {
                    captures.add((IVariableBinding) b);
                }
                return true;
            }
        });
        return captures;
    }

    private String renderTypeBody(TypeDeclaration td) {
        StringBuilder out = new StringBuilder();
        ITypeBinding savedBinding = binding;
        IMethodBinding savedMethod = currentMethod;
        Code savedCode = code;
        binding = td.resolveBinding();
        for (Object o : td.bodyDeclarations()) {
            if (!(o instanceof MethodDeclaration)) continue;
            MethodDeclaration md = (MethodDeclaration) o;
            IMethodBinding mb = md.resolveBinding();
            if (mb == null) continue;
            boolean isVoid = mb.getReturnType().getName().equals("void");
            code = new Code();
            currentMethod = mb;
            if (!mb.isConstructor()) {
                write("%s %s(", isVoid ? "void" : code.ptr(mb.getReturnType()),
                        Mapper.instance.mapMethodName(mb.getName()));
            }
            else {
                write("%s(", cnameOf(binding));
            }
            for (int i = 0; i < md.parameters().size(); i++) {
                SingleVariableDeclaration param = (SingleVariableDeclaration) md.parameters().get(i);
                if (i > 0) write(", ");
                write("%s %s", code.ptr(param.getType().resolveBinding()),
                        Mapper.instance.mapParamName(param.getName().getIdentifier()));
            }
            write(")");
            if (md.getBody() != null) md.getBody().accept(this);
            else write("{}");
            write("\n");
            out.append(code.toString());
        }
        code = savedCode;
        binding = savedBinding;
        currentMethod = savedMethod;
        return out.toString();
    }

    //render the method bodies declared inside an anonymous class as C++ method
    //definitions on the generated subclass. Captured locals are rewritten to the
    //corresponding generated fields (see anonCaptures).
    private String renderAnonyBody(AnonymousClassDeclaration anon, ITypeBinding base) {
        StringBuilder out = new StringBuilder();
        ITypeBinding savedBinding = binding;
        IMethodBinding savedMethod = currentMethod;
        Code savedCode = code;
        binding = anon.resolveBinding();
        if (binding == null) binding = savedBinding;
        for (Object o : anon.bodyDeclarations()) {
            if (!(o instanceof MethodDeclaration)) continue;
            MethodDeclaration md = (MethodDeclaration) o;
            IMethodBinding mb = md.resolveBinding();
            if (mb == null) continue;
            boolean isVoid = mb.getReturnType().getName().equals("void");
            code = new Code();
            currentMethod = mb;
            write("%s %s(", isVoid ? "void" : code.ptr(mb.getReturnType()),
                    Mapper.instance.mapMethodName(mb.getName()));
            write(")");
            if (md.getBody() != null) md.getBody().accept(this);
            else write("{}");
            write("\n");
            out.append(code.toString());
        }
        code = savedCode;
        binding = savedBinding;
        currentMethod = savedMethod;
        return out.toString();
    }

    private boolean isTypeExpression(Expression e) {
        if (!(e instanceof Name)) return false;
        IBinding b = ((Name) e).resolveBinding();
        return b != null && b.getKind() == IBinding.TYPE;
    }

    private List<String> mapped(ClassInstanceCreation node) {
        if (!Config.useMappers) return null;
        IMethodBinding b = node.resolveConstructorBinding();
        if (b == null || b.getDeclaringClass().isFromSource()) return null;
        if (mappedCalls.containsKey(node)) return mappedCalls.get(node);
        List<String> res = Mapper.instance.mapMethod(b, node.arguments(), null, this.binding, null, includes);
        mappedCalls.put(node, res);
        return res;
    }

    private void emitLines(List<String> lines) {
        if (lines == null) return;
        for (int i = 0; i < lines.size() - 1; i++) {
            code.line("%s\n", lines.get(i));
        }
    }

    private void emit(ASTNode node, List<String> lines) {
        if (!preambleEmitted.contains(node)) {
            emitLines(lines);
        }
        code.write(lines.get(lines.size() - 1));
    }

    //multi-line mappings have to be emitted before the statement containing the call
    private void emitPreamble(Statement stmt) {
        stmt.accept(new ASTVisitor() {
            @Override
            public boolean preVisit2(ASTNode node) {
                return !(node instanceof Statement) || node == stmt;
            }

            @Override
            public boolean visit(MethodInvocation node) {
                emitLines(mapped(node));
                preambleEmitted.add(node);
                return true;
            }

            @Override
            public boolean visit(ClassInstanceCreation node) {
                emitLines(mapped(node));
                preambleEmitted.add(node);
                return true;
            }
        });
    }

    public void all(Path rel) {
        //each source needs its own declarations to compile standalone
        for (Object o : cu.types()) {
            ITypeBinding type = ((AbstractTypeDeclaration) o).resolveBinding();
            if (type == null) continue;
            selfHeaders.add(type.getBinaryName().replace("$", "_").replace(".", "/") + ".h");
        }
        if (cu.getPackage() != null) {
            String ns = cu.getPackage().getName().toString().replace(".", "::");
            code.usings.add(ns);
            code.write("using namespace %s;\n", ns);
        }
        code.write("\n");
        for (Object o : cu.types()) {
            AbstractTypeDeclaration decl = (AbstractTypeDeclaration) o;
            visit(decl);
        }
        String name = rel.toString();
        name = name.substring(0, name.length() - 5) + ".cpp";
        Path file = Paths.get(dir.toString(), name);
        try {
            Files.createDirectories(file.getParent());
            StringBuilder out = new StringBuilder();
            if (Config.common_forwards && Config.include_common_forwards) {
                out.append("#include \"common.h\"\n");
            }
            for (String inc : includes) {
                out.append(new IncludeStmt(inc)).append("\n");
            }
            //the source's own header goes last so its dependencies are already declared
            for (String inc : selfHeaders) {
                out.append(new IncludeStmt(inc)).append("\n");
            }
            out.append(anonyDecls);
            out.append(code.toString());
            Files.write(file, out.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //define enum constants and initialize them before main via a namespace-scope initializer
    private void emitEnumStaticInit(EnumDeclaration node) {
        ITypeBinding binding = node.resolveBinding();
        if (binding == null || node.enumConstants().isEmpty()) return;
        String cls = code.str(binding);
        if (binding.getBinaryName() != null) {
            includes.add(binding.getBinaryName().replace("$", "_").replace(".", "/") + ".h");
        }
        ITypeBinding saved = this.binding;
        this.binding = binding;
        code.line("//enum %s\n", cls);
        for (Object o : node.enumConstants()) {
            EnumConstantDeclaration c = (EnumConstantDeclaration) o;
            code.line("%s* %s::%s = nullptr;\n", cls, cls, c.getName());
        }
        String init = cls + "_init_enum";
        code.line("static bool %s() {\n", init);
        code.up();
        int ordinal = 0;
        for (Object o : node.enumConstants()) {
            EnumConstantDeclaration c = (EnumConstantDeclaration) o;
            code.write("%s::%s = new %s(", cls, c.getName(), cls);
            args((List<Expression>) c.arguments());
            code.write(");\n");
            code.line("%s::%s->_ordinal = %d;\n", cls, c.getName(), ordinal++);
            String nameInit = Config.useMappers
                    ? "new std::string(\"" + c.getName() + "\")"
                    : "new java::lang::String(" + wcharVector("\"" + c.getName() + "\"") + ")";
            code.line("%s::%s->_name = %s;\n", cls, c.getName(), nameInit);
        }
        code.line("    return true;\n}\n");
        code.line("static bool %s_done = %s();\n", init, init);
        this.binding = saved;
    }

    //constructors and methods declared in an enum body need out-of-line definitions too
    private void emitEnumMembers(EnumDeclaration node) {
        ITypeBinding saved = this.binding;
        this.binding = node.resolveBinding();
        for (Object o : node.bodyDeclarations()) {
            if (o instanceof MethodDeclaration) {
                visit((MethodDeclaration) o);
            }
        }
        this.binding = saved;
    }

    public void visit(AbstractTypeDeclaration decl) {
        //includes
        if (decl instanceof EnumDeclaration) {
            emitEnumStaticInit((EnumDeclaration) decl);
            emitEnumMembers((EnumDeclaration) decl);
        }
        else if (decl instanceof AnnotationTypeDeclaration) {
            //visit((AnnotationTypeDeclaration) decl, null);
        }
        else if (decl instanceof RecordDeclaration) {
            visit((RecordDeclaration) decl);
        }
        else {
            visit((TypeDeclaration) decl, null);
        }
    }

    //record constructors/accessors are emitted inline in the header; only
    //explicitly declared methods need their bodies emitted here
    public boolean visit(RecordDeclaration rec) {
        ITypeBinding savedBinding = binding;
        binding = rec.resolveBinding();
        recordComponents.clear();
        for (Object o : rec.recordComponents()) {
            recordComponents.add(((SingleVariableDeclaration) o).getName().getIdentifier());
        }
        for (Object o : rec.bodyDeclarations()) {
            if (o instanceof MethodDeclaration && ((MethodDeclaration) o).getBody() != null) {
                visit((MethodDeclaration) o);
            }
        }
        recordComponents.clear();
        binding = savedBinding;
        return false;
    }

    public void visit(TypeDeclaration node, ITypeBinding outer) {
        this.binding = node.resolveBinding();
        code.write("//class %s\n", node.getName());
        //static fields
        for (Object o : node.bodyDeclarations()) {
            if (o instanceof FieldDeclaration) {
                FieldDeclaration fd = (FieldDeclaration) o;
                ITypeBinding type = fd.getType().resolveBinding();
                if (Modifier.isStatic(fd.getModifiers())) {
                    for (VariableDeclarationFragment frag : (List<VariableDeclarationFragment>) fd.fragments()) {
                        if (frag.getInitializer() == null) continue;
                        if (Modifier.isFinal(fd.getModifiers()) && !type.isPrimitive() && Config.static_field_cofui) {
                            //header declares a cofui accessor; define it here with a lazy value
                            code.write("%s %s::%s() { static %s v = ", code.ptr(type), this.binding,
                                    frag.getName(), code.ptr(type));
                            frag.getInitializer().accept(this);
                            code.write("; return v; }\n");
                        }
                        else if (!Modifier.isFinal(fd.getModifiers())) {
                            code.write("%s %s::%s = ", code.ptr(type), this.binding, frag.getName());
                            frag.getInitializer().accept(this);
                            code.write(";\n");
                        }
                    }
                }
            }
        }
        for (Object o : node.bodyDeclarations()) {
            if (o instanceof MethodDeclaration) {
                visit((MethodDeclaration) o);
            }
            else if (o instanceof TypeDeclaration) {
                //emit inner class method definitions in this source too
                ITypeBinding nested = ((TypeDeclaration) o).resolveBinding();
                if (nested != null && nested.getBinaryName() != null) {
                    includes.add(nested.getBinaryName().replace("$", "_").replace(".", "/") + ".h");
                }
                ITypeBinding savedOuter = this.binding;
                visit((TypeDeclaration) o, savedOuter);
                this.binding = savedOuter;
            }
            else if (o instanceof RecordDeclaration) {
                ITypeBinding recType = ((RecordDeclaration) o).resolveBinding();
                if (recType != null && recType.getBinaryName() != null) {
                    includes.add(recType.getBinaryName().replace("$", "_").replace(".", "/") + ".h");
                }
                ITypeBinding savedOuter = this.binding;
                visit((RecordDeclaration) o);
                this.binding = savedOuter;
            }
            else if (o instanceof EnumDeclaration) {
                emitEnumStaticInit((EnumDeclaration) o);
                emitEnumMembers((EnumDeclaration) o);
            }
            else if (o instanceof Initializer && Modifier.isStatic(((Initializer) o).getModifiers())) {
                code.write("void %s::si()", this.binding);
                ((Initializer) o).getBody().accept(this);
                code.write("\n");
            }
        }
    }

    public boolean visit(MethodDeclaration node) {
        if (node.getBody() == null) return false;
        IMethodBinding b = node.resolveBinding();
        currentMethod = b;
        List<ITypeBinding> methodParams = TypeHelper.methodTypeParams(b, this.binding);
        TypeVisitor.Wildcards wildcards = new TypeVisitor.Wildcards();
        for (Object o : node.parameters()) {
            TypeVisitor.collectWildcards(((SingleVariableDeclaration) o).getType().resolveBinding(), wildcards);
        }
        boolean oldKeep = TypeVisitor.keepTypeVars;
        Set<String> oldVars = TypeVisitor.activeTypeVars;
        TypeVisitor.activeTypeVars = new HashSet<>();
        TypeVisitor.keepTypeVars = !methodParams.isEmpty() && Config.useMappers;
        for (ITypeBinding p : methodParams) {
            TypeVisitor.activeTypeVars.add(p.getName());
        }
        if (!wildcards.isEmpty()) {
            code.noteLine("wildcard type arguments templated: " + wildcards.describe());
        }
        else if (!methodParams.isEmpty()) {
            code.noteLine("method type parameters kept as C++ template: " + TypeHelper.templateLine(methodParams));
        }
        else {
            String erasure = TypeHelper.erasureNote(b);
            if (erasure != null) {
                code.noteLine(erasure);
            }
        }
        //class template parameters first, then the method's own parameters
        ITypeBinding[] classParams = this.binding == null ? new ITypeBinding[0] : this.binding.getTypeParameters();
        if (classParams.length > 0) {
            StringBuilder template = new StringBuilder("template <");
            for (int i = 0; i < classParams.length; i++) {
                if (i > 0) template.append(", ");
                template.append("class ").append(classParams[i].getName());
            }
            code.write("%s>\n", template);
        }
        List<String> allNames = new ArrayList<>();
        for (ITypeBinding p : methodParams) allNames.add(p.getName());
        allNames.addAll(wildcards.names);
        String methodTemplate = TypeHelper.templateLineForNames(allNames);
        if (methodTemplate != null) {
            code.write("%s\n", methodTemplate);
        }
        if (b.isConstructor()) {
            code.write("%s::%s(", this.binding, cnameOf(this.binding));
        }
        else {
            code.write("%s %s::%s(", code.ptr(b.getReturnType()), this.binding, Mapper.instance.mapMethodName(b.getName()));
        }
        for (int i = 0; i < node.parameters().size(); i++) {
            SingleVariableDeclaration param = (SingleVariableDeclaration) node.parameters().get(i);
            ITypeBinding ptype = param.getType().resolveBinding();
            String rendered = wildcards.isEmpty() ? code.ptr(ptype)
                    : code.ptrSmart(TypeVisitor.fromBinding(ptype, null, wildcards));
            code.write("%s %s", rendered, Mapper.instance.mapParamName(param.getName().getIdentifier()));
            if (i < node.parameters().size() - 1) code.write(", ");
        }
        code.write(")");
        if (b.isConstructor() && node.getBody() != null
                && !node.getBody().statements().isEmpty()
                && node.getBody().statements().get(0) instanceof SuperConstructorInvocation) {
            SuperConstructorInvocation sup = (SuperConstructorInvocation) node.getBody().statements().get(0);
            code.write(" : %s(", this.binding.getSuperclass());
            args((List<Expression>) sup.arguments());
            code.write(")");
        }
        emitFallbackReturn = !b.isConstructor() && !b.getReturnType().getName().equals("void");
        currentMethodBody = node.getBody();
        node.getBody().accept(this);
        emitFallbackReturn = false;
        currentMethodBody = null;
        code.write("\n");
        TypeVisitor.keepTypeVars = oldKeep;
        TypeVisitor.activeTypeVars = oldVars;
        return false;
    }


    public void write(String s, Object... args) {
        code.write(s, args);
    }

    @Override
    public boolean visit(Block n) {
        //block = n;
        code.line("{\n");
        code.up();
        for (Statement s : (List<Statement>) n.statements()) {
            emitPreamble(s);
            s.accept(this);
            code.write("\n");
        }
        if (emitFallbackReturn && n == currentMethodBody) {
            //Java requires a return on all paths; C++ still warns when control can
            //reach the end after a try/finally translation
            code.line("return {};");
            emitFallbackReturn = false;
        }
        code.down();
        code.line("}");
        return false;
    }

    @Override
    public boolean visit(TypeDeclarationStatement node) {
        AbstractTypeDeclaration decl = node.getDeclaration();
        if (!(decl instanceof TypeDeclaration)) {
            Logger.log(this.binding, "local declaration is not supported: " + decl.getName());
            return false;
        }
        TypeDeclaration td = (TypeDeclaration) decl;
        ITypeBinding type = td.resolveBinding();
        if (type == null) {
            Logger.log(this.binding, "local class has no binding: " + td.getName());
            return false;
        }
        //generate a named struct in the .cpp, the same way anonymous classes are
        //generated, with captured locals as constructor arguments
        String name = cnameOf(type);
        List<IVariableBinding> captures = collectTypeCaptures(td);
        anonCaptures.clear();
        for (int i = 0; i < captures.size(); i++) {
            anonCaptures.put(captures.get(i), "_cap" + i);
        }
        String body = renderTypeBody(td);
        anonCaptures.clear();

        String ns = cu.getPackage() == null ? "" : cu.getPackage().getName().toString().replace(".", "::");
        boolean needsParent = body.contains(Config.parentName + "->");
        String enclosing = cnameOf(this.binding);
        anonyDecls.append("namespace ").append(ns).append(" {\n");
        if (needsParent) anonyDecls.append("class ").append(enclosing).append(";\n");
        anonyDecls.append("struct ").append(name).append(" : public java::lang::Object {\n");
        for (int i = 0; i < captures.size(); i++) {
            anonyDecls.append("    ").append(code.ptr(captures.get(i).getType())).append(" _cap").append(i).append(";\n");
        }
        if (needsParent) anonyDecls.append("    ").append(enclosing).append("* ").append(Config.parentName).append(";\n");
        anonyDecls.append("    ").append(name).append("(");
        for (int i = 0; i < captures.size(); i++) {
            if (i > 0) anonyDecls.append(", ");
            anonyDecls.append(code.ptr(captures.get(i).getType())).append(" _cap").append(i);
        }
        if (needsParent) {
            if (!captures.isEmpty()) anonyDecls.append(", ");
            anonyDecls.append(enclosing).append("* ").append(Config.parentName);
        }
        anonyDecls.append(")");
        if (needsParent) anonyDecls.append(" : ").append(Config.parentName).append("(").append(Config.parentName).append(")");
        anonyDecls.append(" {\n");
        for (int i = 0; i < captures.size(); i++) {
            anonyDecls.append("        this->_cap").append(i).append(" = _cap").append(i).append(";\n");
        }
        anonyDecls.append("    }\n").append(body).append("};\n");
        anonyDecls.append("}\n\n");

        localClasses.put(type.getBinaryName(), new LocalClass(name, captures, needsParent));
        Logger.log(this.binding, "local class generated as " + name);
        return false;
    }

    @Override
    public boolean visit(AssertStatement node) {
        includes.add("<cassert>");
        code.line("assert(");
        node.getExpression().accept(this);
        code.write(");");
        return false;
    }

    //turn a Java string literal into new std::vector<wchar_t>{L'x', ...}
    private String wcharVector(String literal) {
        StringBuilder sb = new StringBuilder("new std::vector<wchar_t>{");
        int end = literal.length() - 1;
        for (int i = 1; i < end; ) {
            char c = literal.charAt(i);
            String part;
            if (c == '\\') {
                int len = (i + 1 < end && literal.charAt(i + 1) == 'u') ? 6 : 2;
                part = literal.substring(i, Math.min(i + len, end));
                i += len;
            }
            else {
                part = String.valueOf(c);
                i++;
            }
            if (part.equals("'")) part = "\\'";
            sb.append("L'").append(part).append("', ");
        }
        sb.append("}");
        return sb.toString();
    }

    @Override
    public boolean visit(StringLiteral node) {
        CType type = Mapper.instance.mapType(TypeHelper.getStringType(), null);
        if (type.mapped) {
            Mapper.instance.collectIncludes("java.lang.String", includes);
        }
        else {
            addLibInclude(node.resolveTypeBinding());
            //stub String has a char[] constructor, not one taking const char*
            write("new %s(%s)", type, wcharVector(node.getEscapedValue()));
            return false;
        }
        write("new %s(%s)", type, node.getEscapedValue());
        return false;
    }

    @Override
    public boolean visit(BooleanLiteral node) {
        write(node.toString());
        return true;
    }

    @Override
    public boolean visit(CharacterLiteral node) {
        //Java chars become wchar_t in stub mode; C++'L' prefix keeps that type
        if (!Config.useMappers && !Config.type_char.equals("char")) {
            write("L");
        }
        write(node.getEscapedValue());
        //literal.charValue = node.charValue();
        return false;
    }

    @Override
    public boolean visit(NullLiteral node) {
        write("nullptr");
        return true;
    }

    //type.class
    @Override
    public boolean visit(TypeLiteral node) {
        //Class::forName(type)
        write("%s::forName(\"%s\")", TypeHelper.getClassType(), node.getType().resolveBinding());
        return true;
    }

    @Override
    public boolean visit(EmptyStatement node) {
        return false;
    }

    @Override
    public boolean visit(Assignment node) {
        if (node.getOperator().toString().equals(">>>=")) {
            String left = node.getLeftHandSide().toString();
            CType type = TypeVisitor.fromBinding(node.getLeftHandSide().resolveTypeBinding());
            write(left);
            write(" = ");
            write("(unsigned " + type + ")" + left);
            write(" >> ");
            node.getRightHandSide().accept(this);
        }
        else if (node.getOperator().toString().equals("+=") && isStr(node.getLeftHandSide())) {
            //String += is concatenation, but += on std::string* pointers is not valid C++
            includes.add("CppHelper.h");
            node.getLeftHandSide().accept(this);
            write(" = ");
            if (Config.useMappers) {
                write("CppHelper::concat(");
                node.getLeftHandSide().accept(this);
                write(", ");
                node.getRightHandSide().accept(this);
                write(")");
            }
            else {
                node.getLeftHandSide().accept(this);
                concatPart(node.getRightHandSide());
            }
        }
        else {
            node.getLeftHandSide().accept(this);
            write(node.getOperator().toString());
            node.getRightHandSide().accept(this);
        }
        return false;
    }

    //when emitting a non-primitive switch, a break targeting it exits the dispatch
    private boolean breakTargetsSwitch(BreakStatement node) {
        if (switchNode == null) return false;
        ASTNode cur = node.getParent();
        while (cur != null) {
            if (cur == switchNode) return true;
            if (cur instanceof ForStatement || cur instanceof WhileStatement || cur instanceof DoStatement
                    || cur instanceof EnhancedForStatement || cur instanceof SwitchStatement) {
                return false;
            }
            cur = cur.getParent();
        }
        return false;
    }

    @Override
    public boolean visit(BreakStatement node) {
        if (node.getLabel() != null) {
            code.line("goto %s;", node.getLabel().getIdentifier());
            return false;
        }
        if (breakTargetsSwitch(node)) {
            code.line("goto %s;", switchEndLabel);
            return false;
        }
        code.line("break");
        if (node.getLabel() != null) {
            write(" ");
            write(node.getLabel().getIdentifier());
        }
        code.write(";");
        return false;
    }

    @Override
    public boolean visit(ContinueStatement node) {
        code.line("continue");
        if (node.getLabel() != null) {
            write(" ");
            write(node.getLabel().getIdentifier());
        }
        code.write(";");
        return false;
    }

    @Override
    public boolean visit(ReturnStatement node) {
        if (tryLambdaVoidMethod && node.getExpression() == null) {
            code.line("return true;");
            return false;
        }
        code.line("return ");
        if (node.getExpression() != null) {
            writeValue(node.getExpression(), currentMethod == null ? null : currentMethod.getReturnType());
        }
        code.write(";");
        return false;
    }

    @Override
    public boolean visit(ThrowStatement node) {
        code.line("throw ");
        Expression e = node.getExpression();
        //throw a temporary directly; the old *(new X(...)) leaked the allocation
        if (e instanceof ClassInstanceCreation) {
            throwDirect = true;
            e.accept(this);
            throwDirect = false;
        }
        else
            //if throwing custom exception
            if (e instanceof Name && !e.toString().equals(catchName)) {
                write("*");
                e.accept(this);
            }
            else {
                Logger.log(this.binding, "throw of a non-reference expression is not supported: " + node);
                e.accept(this);
            }
        write(";");
        return false;
    }

    //declare try-with-resources values and close them on scope exit (also on exceptions)
    private void emitResources(TryStatement node) {
        if (node.resources().isEmpty()) return;
        includes.add("CppHelper.h");
        for (Object o : node.resources()) {
            VariableDeclarationExpression res = (VariableDeclarationExpression) o;
            res.accept(this);
            for (Object f : res.fragments()) {
                VariableDeclarationFragment frag = (VariableDeclarationFragment) f;
                code.line("CppHelper::ScopeGuard _sg%d([&](){ %s->close(); });\n",
                        resGuardCnt++, frag.getName());
            }
        }
    }

    @Override
    public boolean visit(TryStatement node) {
        boolean hasResources = !node.resources().isEmpty();
        Block finallyBody = node.getFinally();
        if (finallyBody == null) {
            if (hasResources) {
                code.line("{\n");
                emitResources(node);
            }
            emitTryCatch(node);
            if (hasResources) {
                code.line("}\n");
            }
            return false;
        }

        //Java finally runs on every exit and a return/throw in finally overrides the
        //try result. The try/catch and finally parts are passed as lambdas that
        //return std::optional<R> (or bool for void); an empty optional means the
        //try completed normally without returning.
        includes.add("CppHelper.h");
        ITypeBinding ret = currentMethod == null ? null : currentMethod.getReturnType();
        boolean isVoid = ret == null || ret.getName().equals("void");
        if (isVoid) {
            write("if (j2cpp::try_finally_void(\n");
            write("[&]() -> bool {\n");
            //resources belong to the try part so their guards close before finally
            if (hasResources) {
                emitResources(node);
            }
            tryLambdaVoidMethod = true;
            emitTryCatch(node);
            tryLambdaVoidMethod = false;
            write("\nreturn false;\n},\n");
            write("[&]() {\n");
            finallyBody.accept(this);
            write("\n})) return;\n");
        }
        else {
            String ortype = "std::optional<" + code.ptr(ret) + ">";
            write("{\nauto _tf = j2cpp::try_finally_value(\n");
            write("[&]() -> %s {\n", ortype);
            //resources belong to the try part so their guards close before finally
            if (hasResources) {
                emitResources(node);
            }
            emitTryCatch(node);
            write("\nreturn std::nullopt;\n},\n");
            write("[&]() -> %s {\n", ortype);
            finallyBody.accept(this);
            write("\nreturn std::nullopt;\n});\n");
            write("if (_tf.has_value()) return *_tf;\n}\n");
        }
        return false;
    }

    private void emitTryCatch(TryStatement node) {
        if (node.catchClauses().isEmpty()) {
            //no catch clause: emit a plain block so exceptions propagate to the
            //helper, which runs finally and rethrows (Java try-finally semantics)
            node.getBody().accept(this);
            return;
        }
        write("try ");
        node.getBody().accept(this);
        for (Object o : node.catchClauses()) {
            CatchClause clause = (CatchClause) o;
            String savedCatch = catchName;
            String catchVar = Mapper.instance.mapParamName(clause.getException().getName().getIdentifier());
            catchName = catchVar;
            write(" catch(");
            ITypeBinding type = clause.getException().getType().resolveBinding();
            addLibInclude(type);
            write("%s& %s) ", type == null ? clause.getException().getType() : type, catchVar);
            clause.getBody().accept(this);
            catchName = savedCatch;
        }
    }

    @Override
    public boolean visit(IfStatement node) {
        code.line("if(");
        node.getExpression().accept(this);
        code.write(")");
        node.getThenStatement().accept(this);
        code.line("");
        if (node.getElseStatement() != null) {
            code.write("else ");
            node.getElseStatement().accept(this);
        }
        return false;
    }

    @Override
    public boolean visit(ForStatement node) {
        code.line("for(");
        boolean savedForInit = forInit;
        forInit = true;
        int i = 0;
        for (Expression init : (List<Expression>) node.initializers()) {
            if (i > 0) write(" ");
            init.accept(this);
            i++;
        }
        forInit = savedForInit;
        code.write(";");
        if (node.getExpression() != null) {
            node.getExpression().accept(this);
        }
        code.write(";");
        i = 0;
        for (Expression updater : (List<Expression>) node.updaters()) {
            if (i > 0) code.write(", ");
            updater.accept(this);
            i++;
        }
        code.write(")");
        node.getBody().accept(this);
        return false;
    }

    //box a primitive element when the loop variable needs a reference type
    private String boxedElement(String raw, ITypeBinding target, boolean elemPrimitive) {
        if (target == null || target.isPrimitive()) return raw;
        if (Config.useMappers) {
            if ("java.lang.Number".equals(target.getQualifiedName())) {
                //the element type may be a wildcard/template parameter, so box generically
                includes.add("CppHelper.h");
                return "CppHelper::boxNumber(" + raw + ")";
            }
            return raw;
        }
        if (!elemPrimitive) return raw;
        String wrapper = wrapperName(target.getName());
        if (wrapper != null) {
            includes.add("lib/java/lang/" + wrapper + ".h");
            return "new java::lang::" + wrapper + "(" + raw + ")";
        }
        return raw;
    }

    @Override
    public boolean visit(EnhancedForStatement node) {
        code.line("{");
        code.up();
        ITypeBinding bind = node.getExpression().resolveTypeBinding();
        String ve = "var" + (forVarCnt++);
        //evaluate the array/iterable expression exactly once; auto keeps
        //template/wildcard element types intact
        write("auto %s = ", ve);
        node.getExpression().accept(this);
        code.write(";\n");

        SingleVariableDeclaration v = node.getParameter();
        ITypeBinding type = v.getType().resolveBinding();
        ITypeBinding elem = bind.isArray() ? bind.getElementType()
                : (bind.getTypeArguments().length > 0 ? bind.getTypeArguments()[0] : null);
        boolean elemPrimitive = elem != null && (elem.isPrimitive()
                || (Config.useMappers && Mapper.instance.mapType(elem).isPrim()));
        if (bind.isArray()) {
            String i = "i_" + forVarCnt++;
            code.line("for(int %s = 0; %s < %s->size(); %s++){", i, i, ve, i);
            code.up();
            code.line("%s %s = %s;", code.ptr(type), v.getName().toString(),
                    boxedElement(ve + "->at(" + i + ")", type, elemPrimitive));
            bodyNoFirst(node.getBody());
            code.down();
            code.line("}");
        }
        else {
            CType mapped = Mapper.instance.mapType(bind);
            String container = mapped.type;
            if (Config.useMappers && mapped.mapped
                    && (container.equals("vector") || container.equals("unordered_set")
                    || container.equals("set") || container.equals("list")
                    || container.equals("deque"))) {
                //mapped Java collections become C++ containers: iterate with begin()/end()
                code.line("for(auto it = %s->begin(); it != %s->end(); ++it){", ve, ve);
                code.up();
                code.line("%s %s = %s;", code.ptr(type), v.getName().toString(),
                        boxedElement("*it", type, elemPrimitive));
                bodyNoFirst(node.getBody());
                code.down();
                code.line("}");
            }
            else {
                //no mapper: use java.util.Iterator, still over the single-evaluation temp
                code.line("for(");
                CType it = new CType("java::util::Iterator");
                write("%s it = %s;\n", code.ptr(it), ve);
                code.write("it->hasNext();){\n");
                code.up();
                code.line("%s %s = (%s)it->next();\n", code.ptr(type), v.getName().toString(), type);
                bodyNoFirst(node.getBody());
                code.down();
                code.line("}\n");
            }
        }
        code.down();
        code.line("}");
        return false;
    }

    void bodyNoFirst(Statement st) {
        if (st instanceof Block) {
            Block b = (Block) st;
            for (Statement s : (List<Statement>) b.statements()) {
                s.accept(this);
            }
        }
        else {
            st.accept(this);
        }
    }

    @Override
    public boolean visit(SingleVariableDeclaration node) {
        if (Config.use_auto && !(node.getParent() instanceof CatchClause)) {
            code.line("auto %s", node.getName().getIdentifier());
        }
        else {
            ITypeBinding type = node.getType().resolveBinding();
            code.line("%s %s", code.ptr(type), node.getName().getIdentifier());
        }
        return false;
    }

    @Override
    public boolean visit(WhileStatement node) {
        code.line("while(");
        node.getExpression().accept(this);
        write(")");
        node.getBody().accept(this);
        return false;
    }

    @Override
    public boolean visit(DoStatement node) {
        code.line("do");
        node.getBody().accept(this);
        code.line("while(");
        node.getExpression().accept(this);
        write(");");
        return false;
    }

    boolean inSwitchExpr;
    String switchExprType;

    @Override
    public boolean visit(SwitchExpression node) {
        ITypeBinding type = node.resolveTypeBinding();
        switchExprType = type == null ? "int" : code.ptr(type);
        //detect type-pattern cases
        List<TypePattern> patterns = new ArrayList<>();
        for (Object o : node.statements()) {
            if (o instanceof SwitchCase) {
                for (Object ex : ((SwitchCase) o).expressions()) {
                    if (ex instanceof TypePattern) patterns.add((TypePattern) ex);
                }
            }
        }
        write("[&]() -> %s {\n", switchExprType);
        if (patterns.isEmpty()) {
            //plain switch expression
            write("switch(");
            node.getExpression().accept(this);
            write(") {\n");
            for (Object o : node.statements()) {
                Statement s = (Statement) o;
                if (s instanceof SwitchCase) {
                    SwitchCase c = (SwitchCase) s;
                    if (c.isDefault()) code.line("default:");
                    else code.line("case %s:", caseExpr(c, node.getExpression().resolveTypeBinding()));
                }
                else s.accept(this);
            }
            code.line("}");
        }
        else {
            //type-pattern switch: emit if/else chain with dynamic_cast bindings
            String sel = "sw" + forVarCnt++;
            write("%s ", code.ptr(node.getExpression().resolveTypeBinding()));
            write("%s = ", sel);
            node.getExpression().accept(this);
            write(";\n");
            boolean first = true;
            Map<String, String> savedCasts = new HashMap<>(patternCasts);
            for (Object o : node.statements()) {
                Statement st = (Statement) o;
                if (!(st instanceof SwitchCase)) continue;
                SwitchCase c = (SwitchCase) st;
                if (c.isDefault()) {
                    write("else { ");
                    emitSwitchArms(node, c);
                    write(" }\n");
                    continue;
                }
                for (Object ex : c.expressions()) {
                    if (ex instanceof TypePattern) {
                        TypePattern tp = (TypePattern) ex;
                        SingleVariableDeclaration var = tp.getPatternVariable();
                        //JDT recovery leaves the pattern variable's binding unresolved;
                        //the pattern's toString() is "Type varName"
                        String patText = tp.toString().trim();
                        int sp = patText.lastIndexOf(' ');
                        String patType = sp > 0 ? patText.substring(0, sp) : patText;
                        String varName = sp > 0 ? patText.substring(sp + 1) : "p";
                        String ns = cu.getPackage() == null ? "" : cu.getPackage().getName().toString().replace(".", "::") + "::";
                        String outer = this.binding == null ? "" : cnameOf(this.binding) + "_";
                        String header = this.binding.getPackage() == null ? "" : this.binding.getPackage().getName().replace('.', '/') + "/";
                        includes.add(header + outer + patType + ".h");
                        write("%s (auto %s = dynamic_cast<%s%s%s*>(%s)) { ",
                                first ? "if" : "else if", varName, ns, outer, patType, sel);
                        patternCasts.put(varName, varName);
                        first = false;
                    }
                }
                emitSwitchArms(node, c);
                write("}\n");
            }
            patternCasts.clear();
            patternCasts.putAll(savedCasts);
            write("return {}; ");
        }
        code.line("}()");
        return false;
    }

    //emit the statements belonging to a switch case arm (skip the case label)
    private void emitSwitchArms(SwitchExpression sw, SwitchCase target) {
        List<Statement> stmts = (List<Statement>) sw.statements();
        int idx = stmts.indexOf(target);
        for (int i = idx + 1; i < stmts.size() && !(stmts.get(i) instanceof SwitchCase); i++) {
            Statement s = stmts.get(i);
            if (s instanceof YieldStatement) {
                write("return ");
                Expression e = ((YieldStatement) s).getExpression();
                if (e != null) e.accept(this);
                write("; ");
            }
            else s.accept(this);
        }
    }

    @Override
    public boolean visit(YieldStatement node) {
        write("return ");
        if (node.getExpression() != null) node.getExpression().accept(this);
        write("; ");
        return false;
    }

    //JDT recovery nodes stringify as MISSING; fall back to the case label's own
    //toString (e.g. "case 1:") and strip the keyword/colon
    private String caseExpr(SwitchCase c, ITypeBinding selector) {
        Expression ex = c.getExpression();
        String ct = ex == null ? "" : ex.toString();
        if ("MISSING".equals(ct) || ct.isEmpty()) {
            ct = c.toString();
            if (ct.startsWith("case ")) ct = ct.substring(5);
            if (ct.endsWith(":")) ct = ct.substring(0, ct.length() - 1);
        }
        return caseExpr(ct, selector);
    }

    private String caseExpr(String ct, ITypeBinding selector) {
        int arrow = ct.indexOf("->");
        if (arrow >= 0) ct = ct.substring(0, arrow).trim();
        if (selector != null && selector.isEnum() && !ct.contains("::")) {
            ct = code.str(selector) + "::" + ct;
        }
        return ct;
    }

    @Override
    public boolean visit(SwitchStatement node) {
        ITypeBinding selector = node.getExpression().resolveTypeBinding();
        if (selector != null && selector.isPrimitive()) {
            write("switch(");
            node.getExpression().accept(this);
            write("){\n");
            for (Object o : node.statements()) {
                if (o instanceof SwitchCase) {
                    SwitchCase c = (SwitchCase) o;
                    if (c.isDefault()) {
                        code.line("default:");
                    }
                    else {
                        code.line("case %s: ", caseExpr(c, selector));
                    }
                }
                else {
                    ((Statement) o).accept(this);
                    code.write("\n");
                }
            }
            code.line("}");
            return false;
        }
        //non-primitive selectors (String, enum) need value comparison. Emit a
        //goto-based dispatch so break and fall-through match Java semantics.
        String var = "sw" + forVarCnt++;
        String end = var + "_end";
        String labelBase = var + "_case_";
        String lhs = (selector != null && selector.isEnum()) ? var : "*" + var;
        List<Statement> stmts = (List<Statement>) node.statements();
        String defaultLabel = null;
        int labelCnt = 0;
        for (Statement s : stmts) {
            if (s instanceof SwitchCase) {
                if (((SwitchCase) s).isDefault()) {
                    defaultLabel = labelBase + labelCnt;
                }
                labelCnt++;
            }
        }
        write("{ auto %s = ", var);
        node.getExpression().accept(this);
        write(";\n");
        int dispatchIdx = 0;
        for (Statement s : stmts) {
            if (!(s instanceof SwitchCase)) continue;
            SwitchCase c = (SwitchCase) s;
            String label = labelBase + dispatchIdx++;
            if (c.isDefault()) continue;
            for (Object ex : c.expressions()) {
                write("if (%s == %s) goto %s;\n", lhs,
                        caseExpr(ex.toString(), selector), label);
            }
        }
        write("goto %s;\n", defaultLabel != null ? defaultLabel : end);
        SwitchStatement savedSwitch = switchNode;
        String savedEnd = switchEndLabel;
        switchNode = node;
        switchEndLabel = end;
        int caseIdx = 0;
        for (int i = 0; i < stmts.size(); i++) {
            Statement s = stmts.get(i);
            if (!(s instanceof SwitchCase)) continue;
            code.line("%s%d: {", labelBase, caseIdx++);
            code.up();
            while (i + 1 < stmts.size() && !(stmts.get(i + 1) instanceof SwitchCase)) {
                stmts.get(++i).accept(this);
            }
            code.down();
            code.line("}");
        }
        switchNode = savedSwitch;
        switchEndLabel = savedEnd;
        code.line("%s: ;\n}", end);
        return false;
    }

    @Override
    public boolean visit(LabeledStatement node) {
        code.line("%s:", node.getLabel().getIdentifier());
        return false;
    }

    @Override
    public boolean visit(ExpressionStatement node) {
        code.line("");
        node.getExpression().accept(this);
        code.write(";");
        return false;
    }

    @Override
    public boolean visit(ConstructorInvocation node) {
        return false;
    }

    @Override
    public boolean visit(SuperConstructorInvocation node) {
        return false;
    }

    @Override
    public boolean visit(SynchronizedStatement node) {
        Logger.log(this.binding, "synchronized is not supported: " + node);
        code.line("/* synchronized ");
        node.getExpression().accept(this);
        code.write(" */");
        node.getBody().accept(this);
        return false;
    }

    @Override
    public boolean visit(VariableDeclarationStatement node) {
        ITypeBinding type = node.getType().resolveBinding();
        for (VariableDeclarationFragment frag : (List<VariableDeclarationFragment>) node.fragments()) {
            if (frag.getInitializer() == null) {
                code.line("%s %s;", code.ptr(type), frag.getName());
            }
            else {
                if (Config.use_auto && type.equals(frag.getInitializer().resolveTypeBinding())) {
                    code.line("auto %s = ", frag.getName());
                }
                else {
                    code.line("%s %s = ", code.ptr(type), frag.getName());
                }
                emitInitializer(frag.getInitializer(), type);
                write(";");
            }
        }
        return false;
    }

    //a bare {..} initializer would be a std::initializer_list, not a vector
    private void emitInitializer(Expression init, ITypeBinding target) {
        if (Config.useMappers && init instanceof ArrayInitializer) {
            includes.add("CppHelper.h");
            write("arrays::from(");
            init.accept(this);
            write(")");
        }
        else {
            writeValue(init, target);
        }
    }

    @Override
    public boolean visit(VariableDeclarationExpression node) {
        ITypeBinding type = node.getType().resolveBinding();
        List<VariableDeclarationFragment> frags = (List<VariableDeclarationFragment>) node.fragments();
        for (int idx = 0; idx < frags.size(); idx++) {
            VariableDeclarationFragment frag = frags.get(idx);
            if (frag.getInitializer() == null) {
                code.line("%s %s%s", code.ptr(type), frag.getName(), forInit ? "" : ";");
            }
            else {
                if (Config.use_auto && type.equals(frag.getInitializer().resolveTypeBinding())) {
                    code.line("auto %s = ", frag.getName());
                }
                else {
                    code.line("%s %s = ", code.ptr(type), frag.getName());
                }
                emitInitializer(frag.getInitializer(), type);
                if (forInit) {
                    if (idx < frags.size() - 1) write(", ");
                }
                else {
                    write(";");
                }
            }
        }
        return false;
    }

    @Override
    public boolean visit(FieldAccess node) {
        IVariableBinding binding = node.resolveFieldBinding();
        if (binding != null && Config.writeLibHeader && binding.isField()) {
            LibHandler.instance.addField(binding);
        }
        Expression scope = node.getExpression();
        ITypeBinding typeBinding = scope.resolveTypeBinding();
        if (typeBinding == null) {
            Logger.logBinding(this.binding, node.toString());
            scope.accept(this);
            write("->%s", Mapper.instance.mapFieldName(node.getName().getIdentifier(), this.binding));
            return false;
        }
        else if (typeBinding.isArray() && node.getName().getIdentifier().equals("length")) {
            scope.accept(this);
            write("->size()");
            return false;
        }

        if (binding.isEnumConstant() || Modifier.isStatic(binding.getModifiers())) {
            if (writeMappedField(binding)) return false;
            scope.accept(this);
            write("::%s", Mapper.instance.mapFieldName(node.getName().getIdentifier(), this.binding));
            return false;
        }

        scope.accept(this);
        write("->%s", Mapper.instance.mapFieldName(node.getName().getIdentifier(), this.binding));
        return false;
    }

    //q.new Inner() passes q as the enclosing instance; a bare new Inner() uses this
    private void writeEnclosingInstance(ClassInstanceCreation node) {
        if (node.getExpression() != null) {
            node.getExpression().accept(this);
        }
        else {
            write("this");
        }
    }

    @Override
    public boolean visit(ClassInstanceCreation node) {
        boolean direct = throwDirect;
        throwDirect = false;//nested new expressions are unaffected
        ITypeBinding binding = node.getType().resolveBinding();
        if (binding == null) {
            Logger.logBinding(this.binding, node.toString());
            write(direct ? "%s(" : "new %s(", node.getType());
            args(node.arguments());
            write(")");
            return false;
        }
        addLibInclude(binding);
        if (binding.isFromSource() && binding.getBinaryName() != null) {
            includes.add(binding.getBinaryName().replace("$", "_").replace(".", "/") + ".h");
        }
        if (!binding.isFromSource() && Config.writeLibHeader && node.resolveConstructorBinding() != null) {
            LibHandler.instance.addMethod(node.resolveConstructorBinding());
        }
        List<String> mapped = mapped(node);
        if (mapped != null) {
            if (direct) {
                //mapper targets may start with new; a throw needs a temporary
                mapped = new ArrayList<>(mapped);
                int last = mapped.size() - 1;
                String line = mapped.get(last);
                if (line.startsWith("new ")) {
                    mapped.set(last, line.substring(4));
                }
            }
            emit(node, mapped);
            return false;
        }
        boolean inner = node.getAnonymousClassDeclaration() != null || !Modifier.isStatic(binding.getModifiers()) && binding.isNested();
        if (node.getAnonymousClassDeclaration() == null) {
            LocalClass lc = binding.getBinaryName() == null ? null : localClasses.get(binding.getBinaryName());
            if (lc != null) {
                //local class: pass captured values (and enclosing instance) to the generated struct
                includes.remove(binding.getBinaryName().replace("$", "_").replace(".", "/") + ".h");
                write(direct ? "%s(" : "new %s(", lc.name);
                for (int i = 0; i < lc.captures.size(); i++) {
                    if (i > 0) write(", ");
                    write(lc.captures.get(i).getName());
                }
                if (lc.needsParent) {
                    if (!lc.captures.isEmpty()) write(", ");
                    writeEnclosingInstance(node);
                }
                if ((!lc.captures.isEmpty() || lc.needsParent) && !node.arguments().isEmpty()) write(", ");
                args(node.arguments());
                write(")");
                return false;
            }
            write(direct ? "%s(" : "new %s(", node.getType().resolveBinding());
            args(node.arguments());
            if (inner) {
                if (!node.arguments().isEmpty()) write(", ");
                writeEnclosingInstance(node);
            }
            write(")");
        }
        else {
            //anonymous bodies are dropped: emit a named subclass of the base type
            String name = "Anon" + anonyCnt++;
            if (binding.isFromSource() && binding.getBinaryName() != null) {
                includes.add(binding.getBinaryName().replace("$", "_").replace(".", "/") + ".h");
            }
            else {
                addLibInclude(binding);
            }
            String ns = cu.getPackage() == null ? "" : cu.getPackage().getName().toString().replace(".", "::");
            String baseType = code.str(binding);
            includes.add("<utility>");
            //collect effectively-final locals captured by the anonymous body
            List<IVariableBinding> captures = collectCaptures(node.getAnonymousClassDeclaration());
            anonCaptures.clear();
            for (int i = 0; i < captures.size(); i++) {
                anonCaptures.put(captures.get(i), "_cap" + i);
            }
            //render the anonymous class body's methods into the generated subclass
            String body = renderAnonyBody(node.getAnonymousClassDeclaration(), binding);
            anonCaptures.clear();

            //anon bodies may reference fields of the enclosing class via _parent_ref
            boolean needsParent = body.contains(Config.parentName + "->");
            String enclosing = this.binding == null ? null : cnameOf(this.binding);
            anonyDecls.append("namespace ").append(ns).append(" {\n");
            if (needsParent && enclosing != null) {
                anonyDecls.append("class ").append(enclosing).append(";\n");
            }
            anonyDecls.append("struct ").append(name).append(" : public ").append(baseType).append(" {\n");
            for (int i = 0; i < captures.size(); i++) {
                IVariableBinding cap = captures.get(i);
                anonyDecls.append("    ").append(code.ptr(cap.getType())).append(" _cap").append(i).append(";\n");
            }
            if (needsParent && enclosing != null) {
                anonyDecls.append("    ").append(enclosing).append("* ").append(Config.parentName).append(";\n");
            }
            anonyDecls.append("    template <typename... Args> ").append(name).append("(");
            for (int i = 0; i < captures.size(); i++) {
                if (i > 0) anonyDecls.append(", ");
                anonyDecls.append(code.ptr(captures.get(i).getType())).append(" _cap").append(i);
            }
            if (needsParent && enclosing != null) {
                if (!captures.isEmpty()) anonyDecls.append(", ");
                anonyDecls.append(enclosing).append("* ").append(Config.parentName);
            }
            if (!captures.isEmpty() || needsParent) anonyDecls.append(", ");
            anonyDecls.append("Args&&... args) : ").append(baseType)
                    .append("(std::forward<Args>(args)...)");
            for (int i = 0; i < captures.size(); i++) {
                anonyDecls.append(", _cap").append(i).append("(_cap").append(i).append(")");
            }
            if (needsParent && enclosing != null) {
                anonyDecls.append(", ").append(Config.parentName).append("(").append(Config.parentName).append(")");
            }
            anonyDecls.append(" {}\n").append(body).append("};\n");
            anonyDecls.append("}\n\n");
            Logger.log(this.binding, "anonymous class body generated as " + name);
            write(direct ? "%s(" : "new %s(", name);
            //pass captured values first, followed by base constructor arguments
            for (int i = 0; i < captures.size(); i++) {
                if (i > 0) write(", ");
                write(captures.get(i).getName());
            }
            if (needsParent && enclosing != null) {
                if (!captures.isEmpty()) write(", ");
                write("this");
            }
            if ((!captures.isEmpty() || needsParent) && !node.arguments().isEmpty()) write(", ");
            args(node.arguments());
            write(")");
        }
        return false;
    }

    @Override
    public boolean visit(ConditionalExpression node) {
        node.getExpression().accept(this);
        write(" ? ");
        node.getThenExpression().accept(this);
        write(" : ");
        node.getElseExpression().accept(this);
        return false;
    }

    @Override
    public boolean visit(SuperMethodInvocation node) {
        if (node.getQualifier() != null) {
            Logger.log(this.binding, "qualified super method invocation is not supported: " + node);
        }
        code.write("%s::%s(", binding.getSuperclass(), Mapper.instance.mapMethodName(node.getName().getIdentifier()));
        args(node.arguments());
        write(")");
        return false;
    }

    void args(List<Expression> list) {
        args(list, null);
    }

    private static String wrapperName(String primitive) {
        switch (primitive) {
            case "boolean": return "Boolean";
            case "byte": return "Byte";
            case "char": return "Character";
            case "short": return "Short";
            case "int": return "Integer";
            case "long": return "Long";
            case "float": return "Float";
            case "double": return "Double";
            default: return null;
        }
    }

    //in stub mode Java boxing/unboxing must be emitted explicitly
    private void writeValue(Expression e, ITypeBinding target) {
        if (Config.useMappers) {
            ITypeBinding et = e == null ? null : e.resolveTypeBinding();
            if (et != null && et.isPrimitive() && target != null
                    && "java.lang.Number".equals(target.getQualifiedName())) {
                //box a mapped primitive when the target is java.lang.Number
                includes.add("CppHelper.h");
                write("new CppHelper::Number(");
                e.accept(this);
                write(")");
                return;
            }
            if (e != null) e.accept(this);
            return;
        }
        if (e == null || target == null) {
            if (e != null) e.accept(this);
            return;
        }
        ITypeBinding exprType = e.resolveTypeBinding();
        //a generic parameter (e.g. List.add(E)) gives a type variable; infer the
        //wrapper from the primitive expression being passed
        if (exprType != null && exprType.isPrimitive() && target.isTypeVariable()) {
            String wrapper = wrapperName(exprType.getName());
            if (wrapper != null) {
                includes.add("lib/java/lang/" + wrapper + ".h");
                write("new java::lang::%s(", wrapper);
                e.accept(this);
                write(")");
                return;
            }
        }
        if (exprType != null && exprType.isPrimitive() && !target.isPrimitive()) {
            addLibInclude(target);
            write("new %s(", target);
            e.accept(this);
            write(")");
        }
        else if (exprType != null && !exprType.isPrimitive() && target.isPrimitive()) {
            e.accept(this);
            write("->%sValue()", target.getName());
        }
        else {
            e.accept(this);
        }
    }

    void args(List<Expression> list, ITypeBinding[] paramTypes) {
        for (int i = 0; i < list.size(); i++) {
            writeValue(list.get(i), paramTypes != null && i < paramTypes.length ? paramTypes[i] : null);
            if (i < list.size() - 1) write(", ");
        }
    }

    @Override
    public boolean visit(ThisExpression node) {
        if (node.getQualifier() == null) {
            code.write("this");
        }
        else {
            ITypeBinding binding = node.getQualifier().resolveTypeBinding();
            CExpression r = ref2(this.binding, binding);
            if (r != null) {
                code.write(r.toString());
            }
            else {
                Logger.log(this.binding, "qualified this is not supported: " + node);
                code.write("this");
            }
        }
        return false;
    }

    @Override
    public boolean visit(NumberLiteral node) {
        write(node.getToken());
        return false;
    }

    @Override
    public boolean visit(SuperFieldAccess node) {
        if (node.getQualifier() != null) {
            Logger.log(this.binding, "qualified super field access is not supported: " + node);
        }
        code.write("%s::%s", binding.getSuperclass(), node.getName());
        return false;
    }

    @Override
    public boolean visit(InfixExpression node) {
        String op = node.getOperator().toString();
        //if either side is a string, the whole expression is string concatenation
        if (op.equals("+") && (isStr(node.getLeftOperand()) || isStr(node.getRightOperand()))) {
            if (Config.useMappers) {
                code.write("CppHelper::concat(");
                node.getLeftOperand().accept(this);
                code.write(", ");
                node.getRightOperand().accept(this);
                for (Expression e : (List<Expression>) node.extendedOperands()) {
                    code.write(", ");
                    e.accept(this);
                }
                code.write(")");
            }
            else {
                //without mappers Java strings remain objects with a concat method
                writeReceiver(node.getLeftOperand());
                concatPart(node.getRightOperand());
                for (Expression e : (List<Expression>) node.extendedOperands()) {
                    concatPart(e);
                }
            }
            return false;
        }
        if (node.getOperator().toString().equals(">>>")) {
            CType type = TypeVisitor.fromBinding(node.getLeftOperand().resolveTypeBinding());
            write("(unsigned int)");
            node.getLeftOperand().accept(this);
            write(">>");
            node.getRightOperand().accept(this);
            return false;
        }

        node.getLeftOperand().accept(this);
        write(op);
        node.getRightOperand().accept(this);
        if (node.hasExtendedOperands()) {
            for (Expression e : (List<Expression>) node.extendedOperands()) {
                write(op);
                e.accept(this);
            }
        }
        return false;
    }

    boolean isStr(Expression e) {
        ITypeBinding type = e.resolveTypeBinding();
        return type != null && type.getQualifiedName().equals("java.lang.String");
    }

    //mapped constant fields (e.g. Math.PI) emit their literal value
    private boolean writeMappedField(IVariableBinding binding) {
        String value = Mapper.instance.mapFieldValue(binding);
        if (value == null) return false;
        write(value);
        return true;
    }

    //a new-expression receiver needs parentheses before -> or . in C++
    private void writeReceiver(Expression e) {
        boolean paren = e instanceof ClassInstanceCreation
                || (!Config.useMappers && e instanceof StringLiteral);
        if (paren) write("(");
        e.accept(this);
        if (paren) write(")");
    }

    private void concatPart(Expression e) {
        code.write("->concat(");
        if (isStr(e)) {
            e.accept(this);
        }
        else {
            code.write("%s::valueOf(", TypeHelper.getStringType());
            e.accept(this);
            code.write(")");
        }
        code.write(")");
    }

    @Override
    public boolean visit(PostfixExpression node) {
        node.getOperand().accept(this);
        write(node.getOperator().toString());
        return false;
    }

    @Override
    public boolean visit(PrefixExpression node) {
        write(node.getOperator().toString());
        node.getOperand().accept(this);
        return false;
    }

    @Override
    public boolean visit(InstanceofExpression node) {
        code.write("dynamic_cast<%s>(", code.ptr(node.getRightOperand().resolveBinding()));
        node.getLeftOperand().accept(this);
        code.write(")");
        return false;
    }

    @Override
    public boolean visit(ArrayAccess node) {
        node.getArray().accept(this);
        write("->at(");
        node.getIndex().accept(this);
        write(")");
        return false;
    }

    @Override
    public boolean visit(ArrayCreation node) {
        includes.add("CppHelper.h");
        if (node.getInitializer() != null) {
            write("arrays::from(");
            node.getInitializer().accept(this);
            write(")");
        }
        else {
            ITypeBinding elemType = node.getType().getElementType().resolveBinding();
            write("arrays::make%d<%s>(", node.getType().getDimensions(), code.ptr(elemType));
            args(node.dimensions());
            //set missing dims to zero
            for (int i = node.dimensions().size(); i < node.getType().getDimensions(); i++) {
                write(", 0");
            }
            write(")");
        }
        return false;
    }

    //{...}
    @Override
    public boolean visit(ArrayInitializer node) {
        write("{");
        for (Object o : node.expressions()) {
            if (o != node.expressions().get(0)) write(", ");
            ((Expression) o).accept(this);
        }
        write("}");
        return false;
    }

    @Override
    public boolean visit(CastExpression node) {
        ITypeBinding type = node.getType().resolveBinding();
        if (TypeHelper.canCast(node.getExpression(), node.getExpression().resolveTypeBinding(), type)) {
            //System.out.println("static "+clazz.getType());
            //static cast
            write("(%s)", code.ptr(type));
            node.getExpression().accept(this);
        }
        else {
            //System.out.println("dynamic " + clazz.getType());
            //dynamic e.g derived class to base class
            write("dynamic_cast<%s>(", code.ptr(type));
            node.getExpression().accept(this);
            write(")");
        }
        return false;
    }

    @Override
    public boolean visit(ParenthesizedExpression node) {
        write("(");
        node.getExpression().accept(this);
        write(")");
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean visit(MethodInvocation node) {
        IMethodBinding binding = node.resolveMethodBinding();
        if (binding == null) {
            Logger.logBinding(this.binding, node.toString());
            if (node.getExpression() != null) {
                node.getExpression().accept(this);
                write(".");
            }
            write(Mapper.instance.mapMethodName(node.getName().getIdentifier()));
            write("(");
            args(node.arguments());
            write(")");
            return false;
        }
        List<String> mapped = mapped(node);
        if (mapped != null) {
            emit(node, mapped);
            return false;
        }
        if (!binding.getDeclaringClass().isFromSource() && Config.writeLibHeader) {
            LibHandler.instance.addMethod(binding);
        }
        addLibInclude(binding.getDeclaringClass());
        ITypeBinding ret = binding.getReturnType();
        ITypeBinding org = binding.getMethodDeclaration().getReturnType();
        //a generic return type is resolved by C++ template deduction or erasure;
        //an explicit cast is neither needed nor valid in the generated code
        if (!org.equals(ret) && (org.isWildcardType() || org.isTypeVariable())) {
            code.note("generic return type resolved by template deduction/erasure");
        }
        boolean isStatic = Modifier.isStatic(binding.getModifiers());
        if (node.getExpression() == null) {
            ITypeBinding onType = binding.getDeclaringClass();
            //CType type = TypeVisitor.fromBinding(onType, cla);
            if (isStatic) {
                //static method needs qualifier
                code.write(onType);
                write("::");
            }
            else if (this.binding != null && this.binding.getTypeParameters().length > 0
                    && !this.binding.equals(onType)) {
                //dependent base members are not found by unqualified lookup in C++ templates
                write("this->");
            }
            else if (!this.binding.equals(onType) && !this.binding.isSubTypeCompatible(onType)) {
                //method of an enclosing class: qualify through the parent reference
                CExpression ref = ref2(this.binding, onType);
                if (ref != null) {
                    write("%s->", ref);
                }
            }
            else if (Config.qualifyBaseMethod && !this.binding.equals(onType) && this.binding.isSubTypeCompatible(onType) && !onType.isInterface()) {
                //qualify super method,not needed but more precise
                code.write(onType);
                write("::");
            }
            write(Mapper.instance.mapMethodName(node.getName().getIdentifier()));
            write("(");
            args(node.arguments(), binding.getParameterTypes());
            write(")");
        }
        else {
            Expression scope = node.getExpression();
            if (isTypeExpression(scope) && isStatic) {
                code.write(scope.resolveTypeBinding());
                write("::");
            }
            else {
                //new Foo().bar() needs parentheses around the receiver in C++
                boolean paren = scope instanceof ClassInstanceCreation
                        || (!Config.useMappers && scope instanceof StringLiteral);
                if (paren) write("(");
                scope.accept(this);
                if (paren) write(")");
                if (catchName != null && catchName.equals(scope.toString())) {
                    write(".");
                }
                else if (isStatic) {
                    write("::");
                }
                else {
                    write("->");
                }
            }
            write(Mapper.instance.mapMethodName(node.getName().getIdentifier()));
            write("(");
            args(node.arguments(), binding.getParameterTypes());
            write(")");
        }
        return false;
    }

    int refCnt(ITypeBinding cur, ITypeBinding target) {
        int cnt = 0;
        while (true) {
            ITypeBinding parent = cur.getDeclaringClass();
            if (parent == null) return 0;
            cnt++;
            if (parent.equals(target)) {
                return cnt;
            }
            cur = parent;
        }
        //return 0;
    }

    CExpression ref3(ITypeBinding t1, ITypeBinding target) {
        int cnt = refCnt(t1, target);
        if (cnt == 0) return null;
        CExpression res = new CName(Config.parentName);
        cnt--;
        while (cnt-- > 0) {
            res = new CFieldAccess(res, new CName(Config.parentName), true);
        }
        return res;
    }

    //t1 -> t2 -> ... -> target
    //is target outer of t1
    CExpression ref2(ITypeBinding t1, ITypeBinding target) {
        ITypeBinding parent = t1.getDeclaringClass();
        if (parent == null) {
            return null;
        }
        if (parent.equals(target)) {
            return new CName(Config.parentName);
        }
        CExpression ref = ref2(parent, target);
        if (ref == null) {
            return null;
        }
        return new CFieldAccess(ref, new CName(Config.parentName), true);
    }

    @Override
    public boolean visit(SimpleName node) {
        IBinding binding = node.resolveBinding();
        CName name = new CName(node.getIdentifier());
        if (binding == null) {
            Logger.logBinding(this.binding, node.toString());
            write(name.toString());
            return false;
        }
        if (binding.getKind() != IBinding.VARIABLE) {
            //type names can appear as expression qualifiers (e.g. Integer.MAX_VALUE)
            write(name.toString());
            return false;
        }
        boolean isStatic = Modifier.isStatic(binding.getModifiers());
        IVariableBinding variableBinding = (IVariableBinding) binding;
        ITypeBinding onType = variableBinding.getDeclaringClass();
        String captured = anonCaptures.get(variableBinding);
        if (captured != null) {
            write(captured);
            return false;
        }
        if (recordComponents.contains(node.getIdentifier()) && !variableBinding.isParameter()) {
            write("_" + node.getIdentifier());
            return false;
        }
        if (patternCasts.containsKey(node.getIdentifier())) {
            write(node.getIdentifier());
            return false;
        }
        if (variableBinding.isParameter()) {
            write(Mapper.instance.mapParamName(node.getIdentifier()).toString());
            return false;
        }

        if (variableBinding.isField()) {
            name = Mapper.instance.mapFieldName(name.name, this.binding);
            if (isStatic) {
                if (writeMappedField(variableBinding)) return false;
                boolean asMethod = Config.static_field_cofui
                        && Modifier.isFinal(binding.getModifiers())
                        && !variableBinding.getType().isPrimitive()
                        && !variableBinding.isEnumConstant()
                        && onType.isFromSource();
                write("%s::%s%s", onType, name.toString(), asMethod ? "()" : "");
            }
            else {
                if (this.binding.equals(onType)) {
                    write(name.toString());
                    return false;
                }
                if (this.binding.isSubTypeCompatible(onType)) {
                    //super field?
                    write(name.toString());
                    return false;
                }
                CExpression ref = ref2(this.binding, onType);
                if (ref != null) {
                    write("%s->%s", ref, name.toString());
                    return false;
                }
                Logger.logBinding(this.binding, node.toString());
                write(name.toString());
            }
        }
        else {
            //access to catch variable
            if (catchName != null && name.is(catchName) && !(node.getParent() instanceof ThrowStatement)) {
                if (node.getParent() instanceof VariableDeclarationFragment || node.getParent() instanceof Assignment || node.getParent() instanceof ClassInstanceCreation) {
                    write("(&%s)", name.toString());
                    return false;
                }
            }
            if (this.binding.isAnonymous() && onType == null) {
            }
            write(name.toString());
        }
        return false;
    }

    //qualified class,array.length,field access
    @Override
    public boolean visit(QualifiedName node) {
        IBinding binding = node.resolveBinding();
        ITypeBinding typeBinding = node.getQualifier().resolveTypeBinding();

        if ((binding instanceof IVariableBinding) && Config.writeLibHeader) {
            LibHandler.instance.addField((IVariableBinding) binding);
            addLibInclude(((IVariableBinding) binding).getDeclaringClass());
        }
        if (binding == null || typeBinding == null) {
            Logger.logBinding(this.binding, node.toString());
            write(node.getFullyQualifiedName().replace(".", "::"));
            return false;
        }

        String name = node.getName().getIdentifier();

        if (typeBinding.isArray() && name.equals("length")) {
            //array.length
            node.getQualifier().accept(this);
            write("->size()");
            return false;
        }

        boolean isStatic = Modifier.isStatic(binding.getModifiers());

        if (isStatic) {
            IVariableBinding variableBinding = (IVariableBinding) binding;
            if (writeMappedField(variableBinding)) return false;
            CName mapped = Mapper.instance.mapFieldName(name, this.binding);
            if (Config.static_field_cofui && Modifier.isFinal(binding.getModifiers()) && !variableBinding.getType().isPrimitive()
                    && !variableBinding.isEnumConstant() && typeBinding.isFromSource()) {
                write("%s::%s()", typeBinding, mapped);
                return false;
            }
            if (variableBinding.isField()) {
                write("%s::%s", typeBinding, mapped);
                return false;
            }
        }

        if (binding.getKind() == IBinding.VARIABLE) {
            IVariableBinding variableBinding = (IVariableBinding) binding;
            if (variableBinding.isField()) {
                CName mapped = Mapper.instance.mapFieldName(name, this.binding);
                node.getQualifier().accept(this);
                write("->%s", mapped);
                return false;
            }
            else if (variableBinding.isParameter()) {
                String mapped = Mapper.instance.mapParamName(name);
                write("%s", mapped);
            }
        }
        return false;
    }

    String toWrapper(String s) {
        Map<String, String> map = new HashMap<>();
        map.put("boolean", "Boolean");
        map.put("byte", "Byte");
        map.put("char", "Character");
        map.put("short", "Short");
        map.put("int", "Integer");
        map.put("long", "Long");
        map.put("float", "Float");
        map.put("double", "Double");
        return map.get(s);
    }

    String fromWrapper(String s) {
        Map<String, String> map = new HashMap<>();
        map.put("Boolean", "boolean");
        map.put("Byte", "byte");
        map.put("Character", "char");
        map.put("Short", "short");
        map.put("Integer", "int");
        map.put("Long", "lon");
        map.put("Float", "float");
        map.put("Double", "double");
        return map.get(s);
    }


    CExpression box(Expression e, CExpression ce) {
        //Double d = i;
        if (e.resolveBoxing()) {
            ITypeBinding t = e.resolveTypeBinding();
            String type = t.getName();
            //$Wrapper$::valueOf(e)
            CType wr = new CType("java::lang::" + toWrapper(type));
            CMethodInvocation res = new CMethodInvocation(wr, new CName("valueOf"), false);
            res.arguments.add(ce);
            return res;
        }
        if (e.resolveUnboxing()) {
            ITypeBinding t = e.resolveTypeBinding();
            String type = t.getName();
            //e.$prim$Value
            String prim = fromWrapper(type);
            CMethodInvocation res = new CMethodInvocation(ce, new CName(prim + "Value"), true);
            return res;
        }
        return ce;
    }

}