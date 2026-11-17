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
    StringBuilder anonyDecls = new StringBuilder();
    //includes required by mapped methods, emitted before the generated body
    Set<String> includes = new LinkedHashSet<>();
    Set<String> selfHeaders = new LinkedHashSet<>();
    Set<ASTNode> preambleEmitted = new LinkedHashSet<>();
    Map<ASTNode, List<String>> mappedCalls = new HashMap<>();

    public SourceVisitor(Path dir, CompilationUnit cu) {
        this.dir = dir;
        this.cu = cu;
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

    //true if the method references a variable declared outside itself (a captured
    //local or an enclosing field); such bodies cannot be copied into an anon subclass
    private boolean referencesEnclosingState(MethodDeclaration md) {
        Set<IVariableBinding> local = new HashSet<>();
        for (Object p : md.parameters()) {
            IBinding b = ((SingleVariableDeclaration) p).resolveBinding();
            if (b instanceof IVariableBinding) local.add((IVariableBinding) b);
        }
        md.accept(new ASTVisitor() {
            @Override
            public boolean visit(VariableDeclarationFragment n) {
                if (n.resolveBinding() instanceof IVariableBinding) local.add((IVariableBinding) n.resolveBinding());
                return true;
            }
        });
        final boolean[] found = {false};
        md.accept(new ASTVisitor() {
            @Override
            public boolean visit(SimpleName n) {
                IBinding b = n.resolveBinding();
                if (b instanceof IVariableBinding && !local.contains(b)) found[0] = true;
                return true;
            }
        });
        return found[0];
    }

    //render the method bodies declared inside an anonymous class as C++ method
    //definitions on the generated subclass. Bodies that capture enclosing state
    //are emitted as stubs with a warning instead.
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
            if (referencesEnclosingState(md)) {
                Logger.log(this.binding, "anonymous method captures enclosing state; body replaced by stub: " + md.getName());
                if (md.getBody() != null) write("{}");
                else write("{}");
                if (!isVoid) {
                    write(" return {}; ");
                }
            }
            else if (md.getBody() != null) {
                md.getBody().accept(this);
            }
            else {
                write("{}");
            }
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
        if (b == null) return null;
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

    public void visit(AbstractTypeDeclaration decl) {
        //includes
        if (decl instanceof EnumDeclaration) {
            //visit((EnumDeclaration) decl, null);
        }
        else if (decl instanceof AnnotationTypeDeclaration) {
            //visit((AnnotationTypeDeclaration) decl, null);
        }
        else {
            visit((TypeDeclaration) decl, null);
        }
    }

    public void visit(TypeDeclaration node, ITypeBinding outer) {
        this.binding = node.resolveBinding();
        code.write("//class %s\n", node.getName());
        //static fields
        for (Object o : node.bodyDeclarations()) {
            if (o instanceof FieldDeclaration) {
                FieldDeclaration fd = (FieldDeclaration) o;
                ITypeBinding type = fd.getType().resolveBinding();
                if (Modifier.isStatic(fd.getModifiers()) && !Modifier.isFinal(fd.getModifiers())) {
                    for (VariableDeclarationFragment frag : (List<VariableDeclarationFragment>) fd.fragments()) {
                        if (frag.getInitializer() != null) {
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
        }
    }

    public boolean visit(MethodDeclaration node) {
        if (node.getBody() == null) return false;
        IMethodBinding b = node.resolveBinding();
        currentMethod = b;
        //method type variables are erased in the signature, so only class parameters
        //can appear on the out-of-line template definition
        ITypeBinding[] classParams = this.binding == null ? new ITypeBinding[0] : this.binding.getTypeParameters();
        if (classParams.length > 0) {
            StringBuilder template = new StringBuilder("template <");
            for (int i = 0; i < classParams.length; i++) {
                if (i > 0) template.append(", ");
                template.append("class ").append(classParams[i].getName());
            }
            code.write("%s>\n", template);
        }
        if (b.isConstructor()) {
            code.write("%s::%s(", this.binding, Mapper.instance.mapMethodName(b.getName()));
        }
        else {
            code.write("%s %s::%s(", code.ptr(b.getReturnType()), this.binding, Mapper.instance.mapMethodName(b.getName()));
        }
        for (int i = 0; i < node.parameters().size(); i++) {
            SingleVariableDeclaration param = (SingleVariableDeclaration) node.parameters().get(i);
            code.write("%s %s", code.ptr(param.getType().resolveBinding()), Mapper.instance.mapParamName(param.getName().getIdentifier()));
            if (i < node.parameters().size() - 1) code.write(", ");
        }
        code.write(")");
        emitFallbackReturn = !b.isConstructor() && !b.getReturnType().getName().equals("void");
        currentMethodBody = node.getBody();
        node.getBody().accept(this);
        emitFallbackReturn = false;
        currentMethodBody = null;
        code.write("\n");
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
        Logger.log(this.binding, "local class declaration is not supported: " + node.getDeclaration().getName());
        return false;
    }

    @Override
    public boolean visit(AssertStatement node) {
        Logger.log(this.binding, "assert is not supported: " + node);
        code.line("/* assert " + node.getExpression() + " */");
        return false;
    }

    @Override
    public boolean visit(StringLiteral node) {
        CType type = Mapper.instance.mapType(TypeHelper.getStringType(), null);
        if (type.mapped) {
            Mapper.instance.collectIncludes("java.lang.String", includes);
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
        else {
            node.getLeftHandSide().accept(this);
            write(node.getOperator().toString());
            node.getRightHandSide().accept(this);
        }
        return false;
    }

    @Override
    public boolean visit(BreakStatement node) {
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
            node.getExpression().accept(this);
        }
        code.write(";");
        return false;
    }

    @Override
    public boolean visit(ThrowStatement node) {
        code.line("throw ");
        Expression e = node.getExpression();
        //convert heap allocation into stack allocation
        if (e instanceof ClassInstanceCreation) {
            code.write("*(");
            e.accept(this);
            code.write(")");
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

    @Override
    public boolean visit(TryStatement node) {
        if (!node.resources().isEmpty()) {
            Logger.log(this.binding, "try-with-resources is not supported");
        }
        Block finallyBody = node.getFinally();
        if (finallyBody == null) {
            emitTryCatch(node);
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
        int i = 0;
        for (Expression init : (List<Expression>) node.initializers()) {
            if (i > 0) write(", ");
            init.accept(this);
            i++;
        }
        code.write(";");
        node.getExpression().accept(this);
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

    @Override
    public boolean visit(EnhancedForStatement node) {
        code.line("{");
        code.up();
        ITypeBinding bind = node.getExpression().resolveTypeBinding();
        String ve = "var" + (forVarCnt++);
        if (node.getExpression() instanceof SimpleName) {
            //todo only for other case
            write("%s %s = ", code.ptr(bind), ve);
            node.getExpression().accept(this);
            code.write(";\n");
        }
        else {
            write("%s %s = ", code.ptr(bind), ve);
            node.getExpression().accept(this);
            code.write(";\n");
        }
        code.line("for(");
        SingleVariableDeclaration v = node.getParameter();
        ITypeBinding type = v.getType().resolveBinding();
        if (bind.isArray()) {
            String i = "i_" + forVarCnt++;
            code.write("int %s = 0;%s < %s->size();%s++){", i, i, ve, i);
            code.up();
            code.line("%s %s = %s->at(%s);", code.ptr(type), v.getName().toString(), ve, i);
            bodyNoFirst(node.getBody());
            code.down();
            code.line("}");
        }
        else {
            CType it = new CType("java::util::Iterator");
            //it.typeArgs.add(type);
            write("%s it = ", code.ptr(it));
            node.getExpression().accept(this);
            code.write(";\n");
            code.write("it->hasNext();){\n");
            code.up();
            code.line("%s %s = (%s)it->next();\n", code.ptr(type), v.getName().toString(), type);
            bodyNoFirst(node.getBody());
            code.down();
            code.line("}\n");
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

    @Override
    public boolean visit(YieldStatement node) {
        Logger.log(this.binding, "switch expressions (yield) are not supported: " + node);
        code.line("/* yield ");
        if (node.getExpression() != null) {
            node.getExpression().accept(this);
        }
        code.write(" */");
        return false;
    }

    //JDT recovery nodes stringify as MISSING; fall back to the case label's own
    //toString (e.g. "case 1:") and strip the keyword/colon
    private String caseExpr(SwitchCase c, ITypeBinding selector) {
        String ct = c.getExpression().toString();
        if ("MISSING".equals(ct)) {
            ct = c.toString();
            if (ct.startsWith("case ")) ct = ct.substring(5);
            if (ct.endsWith(":")) ct = ct.substring(0, ct.length() - 1);
        }
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
        //non-primitive selectors (String, enum) need value comparison; use an
        //if/else chain with fall-through semantics approximated by break-free groups
        Logger.log(this.binding, "switch on non-primitive selector is approximate: " + node.getExpression());
        String var = "sw" + forVarCnt++;
        write("{ auto %s = ", var);
        node.getExpression().accept(this);
        write("; bool _matched = false;\n");
        List<Statement> stmts = (List<Statement>) node.statements();
        for (int i = 0; i < stmts.size(); i++) {
            Statement s = stmts.get(i);
            if (s instanceof SwitchCase) {
                SwitchCase c = (SwitchCase) s;
                if (c.getExpression() == null) {
                    code.line("if (!_matched) {");
                }
                else {
                    //enum constants are pointers; String values need dereferencing
                    String lhs = (selector != null && selector.isEnum()) ? var : "*" + var;
                    code.write("if (!_matched && %s == %s) {", lhs, caseExpr(c, selector));
                }
                code.up();
                //emit statements until the next case label
                while (i + 1 < stmts.size() && !(stmts.get(i + 1) instanceof SwitchCase)) {
                    Statement body = stmts.get(++i);
                    if (body instanceof BreakStatement) {
                        Logger.log(this.binding, "break in switch is approximate: " + body);
                        continue;
                    }
                    body.accept(this);
                }
                code.down();
                code.line("}");
            }
        }
            code.line("}");
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
                frag.getInitializer().accept(this);
                write(";");
            }
        }
        return false;
    }

    @Override
    public boolean visit(VariableDeclarationExpression node) {
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
                frag.getInitializer().accept(this);
                write(";");
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
            scope.accept(this);
            write("::%s", Mapper.instance.mapFieldName(node.getName().getIdentifier(), this.binding));
            return false;
        }

        scope.accept(this);
        write("->%s", Mapper.instance.mapFieldName(node.getName().getIdentifier(), this.binding));
        //todo Main.this.field
        return false;
    }

    @Override
    public boolean visit(ClassInstanceCreation node) {
        ITypeBinding binding = node.getType().resolveBinding();
        if (binding == null) {
            Logger.logBinding(this.binding, node.toString());
            write("new %s(", node.getType());
            args(node.arguments());
            write(")");
            return false;
        }
        addLibInclude(binding);
        if (!binding.isFromSource() && Config.writeLibHeader && node.resolveConstructorBinding() != null) {
            LibHandler.instance.addMethod(node.resolveConstructorBinding());
        }
        List<String> mapped = mapped(node);
        if (mapped != null) {
            emit(node, mapped);
            return false;
        }
        boolean inner = node.getAnonymousClassDeclaration() != null || !Modifier.isStatic(binding.getModifiers()) && binding.isNested();
        if (node.getAnonymousClassDeclaration() == null) {
            write("new %s(", node.getType().resolveBinding());
            args(node.arguments());
            if (inner && Config.outer_ref_cons_arg) {
                if (!node.arguments().isEmpty()) write(", ");
                write("this");
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
            //render the anonymous class body's methods into the generated subclass
            String body = renderAnonyBody(node.getAnonymousClassDeclaration(), binding);
            anonyDecls.append("namespace ").append(ns).append(" {\n");
            anonyDecls.append("struct ").append(name).append(" : public ").append(baseType).append(" {\n")
                    .append("    template <typename... Args> ").append(name)
                    .append("(Args&&... args) : ").append(baseType)
                    .append("(std::forward<Args>(args)...) {}\n")
                    .append(body)
                    .append("};\n");
            anonyDecls.append("}\n\n");
            Logger.log(this.binding, "anonymous class body generated as " + name);
            write("new %s(", name);
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
        for (int i = 0; i < list.size(); i++) {
            list.get(i).accept(this);
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
                node.getLeftOperand().accept(this);
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
        boolean needCast = !org.equals(ret) && (org.isWildcardType() || org.isTypeVariable());
        if (needCast) {
            //cast
            write("dynamic_cast<%s>(", code.ptr(ret));
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
            else if (Config.qualifyBaseMethod && !this.binding.equals(onType) && this.binding.isSubTypeCompatible(onType) && !onType.isInterface()) {
                //qualify super method,not needed but more precise
                code.write(onType);
                write("::");
            }
            write(Mapper.instance.mapMethodName(node.getName().getIdentifier()));
            write("(");
            args(node.arguments());
            write(")");
        }
        else {
            Expression scope = node.getExpression();
            if (isTypeExpression(scope) && isStatic) {
                code.write(scope.resolveTypeBinding());
                write("::");
            }
            else {
                scope.accept(this);
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
            args(node.arguments());
            write(")");
        }
        if (needCast) {
            code.write(")");
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
        if (variableBinding.isParameter()) {
            write(Mapper.instance.mapParamName(node.getIdentifier()).toString());
            return false;
        }

        if (variableBinding.isField()) {
            name = Mapper.instance.mapFieldName(name.name, this.binding);
            if (isStatic) {
                if (variableBinding.getType().isPrimitive()) {
                    write("%s::%s", onType, name.toString());
                }
                else {
                    write("%s::%s()", onType, name.toString());
                }
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
            CName mapped = Mapper.instance.mapFieldName(name, this.binding);
            if (Config.static_field_cofui && Modifier.isFinal(binding.getModifiers()) && !variableBinding.getType().isPrimitive()
                    && typeBinding.isFromSource()) {
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

        /*String getTargetType(Expression e){
    	ASTNode p = e.getParent();
    	if(p instanceof VariableDeclarationFragment){
    		VariableDeclarationFragment f = (VariableDeclarationFragment)p;
    		return f.resolveBinding().getType().getName();
    	}
    	else if(p instanceof Assignment){
    		Assignment as = (Assignment)p;
    		return as.getLeftHandSide().resolveTypeBinding().getName();
    	}
   	 else if(p instanceof MethodInvocation){
   	 	MethodInvocation m=(MethodInvocation)p;
    	}
    	else if(p instanceof ClassInstanceCreation){
			ClassInstanceCreation cic=(ClassInstanceCreation)p;
			
		}
    	else if(p instanceof InfixExpression){
    		Logger.log(this.binding, "infix expression used as a boxing conversion: " + p);
			return toWrapper(e.resolveTypeBinding().getName());
		}
    	else if(p instanceof PrefixExpression){
    		PrefixExpression pr = (PrefixExpression)p;
    		return toWrapper(e.resolveTypeBinding().getName());
		}
		else if(p instanceof CastExpression){
			CastExpression ce=(CastExpression)p;
			return ce.getType().toString();
		}
    	else if(p instanceof ArrayAccess){
    		return "int";
    	}
    	else if(p instanceof ArrayCreation){
    		return "int";
    	}
		return null;
	}*/
}