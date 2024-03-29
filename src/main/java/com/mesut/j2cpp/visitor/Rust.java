package com.mesut.j2cpp.visitor;

import com.mesut.j2cpp.Config;
import com.mesut.j2cpp.LibHandler;
import com.mesut.j2cpp.Logger;
import com.mesut.j2cpp.ast.CName;
import com.mesut.j2cpp.cppast.CExpression;
import com.mesut.j2cpp.cppast.expr.CFieldAccess;
import com.mesut.j2cpp.map.Mapper;
import org.eclipse.jdt.core.dom.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@SuppressWarnings("unchecked")
public class Rust extends ASTVisitor {

    Path dir;
    CompilationUnit cu;
    public ITypeBinding binding;
    public IMethodBinding method;
    public Code code = new Code();
    static String baseName = "_base";
    String argsName;

    public Rust(Path dir, CompilationUnit cu) {
        this.dir = dir;
        this.cu = cu;
        code.rust = true;
    }

    public void all(Path rel) {
        deps();
        boolean isMain = false;
        for (var decl : (List<AbstractTypeDeclaration>) cu.types()) {
            visit(decl);
            if (this.binding != null && this.binding.getBinaryName().equals(Config.mainClass)) {
                isMain = true;
            }
        }
        String name = rel.toString();
        Path file;
        if (isMain) {
            file = dir.resolve("main.rs");
        }
        else {
            name = name.substring(0, name.length() - 5) + ".rs";
            file = Paths.get(dir.toString(), name);
        }
        try {
            Files.createDirectories(file.getParent());
            Files.write(file, code.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deps() {
        RustDeps deps = new RustDeps(code);
        code.line("use crate::helper;\n");
        for (var decl : (List<AbstractTypeDeclaration>) cu.types()) {
            deps.handle(decl);
        }
        code.write("\n");
    }

    public void visit(AbstractTypeDeclaration decl) {
        if (decl instanceof EnumDeclaration) {
            //visit((EnumDeclaration) decl, null);
        }
        else if (decl instanceof AnnotationTypeDeclaration) {
            //visit((AnnotationTypeDeclaration) decl, null);
        }
        else {
            visit((TypeDeclaration) decl);
        }
    }

    boolean isCons(int mod, ITypeBinding type, VariableDeclarationFragment frag) {
        return Modifier.isStatic(mod) && Modifier.isFinal(mod) && type.isPrimitive() && frag.getInitializer() != null;
    }

    boolean isStatic(int mod, ITypeBinding type, VariableDeclarationFragment frag) {
        if (!Modifier.isStatic(mod)) return false;
        return !isCons(mod, type, frag) ||
                Modifier.isFinal(mod) && type.isPrimitive() && frag.getInitializer() == null;
    }

    boolean isNormal(int mod, ITypeBinding type, VariableDeclarationFragment frag) {
        return !isStatic(mod, type, frag) && !isCons(mod, type, frag);
    }

    List<VariableDeclarationFragment> collectFields(TypeDeclaration node) {
        var res = new ArrayList<VariableDeclarationFragment>();
        for (Object o : node.bodyDeclarations()) {
            if (!(o instanceof FieldDeclaration)) continue;
            FieldDeclaration fd = (FieldDeclaration) o;
            res.addAll((List<VariableDeclarationFragment>) fd.fragments());
        }
        return res;
    }

    public boolean visit(TypeDeclaration node) {
        this.binding = node.resolveBinding();
        var fields = collectFields(node);
        statics(fields);
        if (node.isInterface()) {
            if (!fields.isEmpty()) {
                statics(fields);
            }
            trait(node);
        }
        else {
            code.line("struct %s{\n", node.getName());
            if (node.getSuperclassType() != null) {
                code.line("pub %s: %s,\n", baseName, node.getSuperclassType().resolveBinding());
            }
            for (var frag : fields) {
                FieldDeclaration fd = (FieldDeclaration) frag.getParent();
                ITypeBinding type = fd.getType().resolveBinding();
                if (isNormal(fd.getModifiers(), type, frag)) {
                    code.line("pub %s: %s,\n", frag.getName(), type);
                }
            }
            code.line("}\n\n");
            impl(node, fields);
            //writeDefault(node, fields);
        }
        return false;
    }

    //group methods by interfaces that overrides
    Map<ITypeBinding, List<MethodDeclaration>> collectMethods(TypeDeclaration node) {
        Map<ITypeBinding, List<MethodDeclaration>> map = new HashMap<>();
        for (var method : node.getMethods()) {
            var iface = isIfaceMethod(method.resolveBinding());
            var key = iface == null ? binding : iface;
            var list = map.getOrDefault(key, new ArrayList<>());
            list.add(method);
            map.put(key, list);
        }
        return map;
    }

    ITypeBinding isIfaceMethod(IMethodBinding methodBinding) {
        for (var iface : binding.getInterfaces()) {
            for (var ifaceMethod : iface.getDeclaredMethods()) {
                if (methodBinding.overrides(ifaceMethod)) {
                    return iface;
                }
            }
        }
        return null;
    }

    void trait(TypeDeclaration node) {
        code.line("trait %s{\n", node.getName());
        for (Object o : node.bodyDeclarations()) {
            if (o instanceof MethodDeclaration md) {
                methodHeader(md);
                code.write(";\n");
            }
        }
        code.line("}\n");
    }

    static boolean isMain(IMethodBinding binding) {
        return binding.getDeclaringClass().isTopLevel() &&
                binding.getName().equals("main") &&
                Modifier.isPublic(binding.getModifiers()) &&
                Modifier.isStatic(binding.getModifiers()) &&
                binding.getReturnType().getName().equals("void") &&
                binding.getParameterTypes().length == 1 &&
                binding.getParameterTypes()[0].isArray() &&
                binding.getParameterTypes()[0].getElementType().getQualifiedName().equals("java.lang.String");
    }

    void impl(TypeDeclaration node, List<VariableDeclarationFragment> fields) {
        var methods = collectMethods(node);
        code.line("impl %s{\n", node.getName());
        for (var frag : fields) {
            FieldDeclaration fd = (FieldDeclaration) frag.getParent();
            ITypeBinding type = fd.getType().resolveBinding();
            if (isCons(fd.getModifiers(), type, frag)) {
                code.line("pub const %s: %s = ", frag.getName(), type);
                frag.getInitializer().accept(this);
                code.write(";\n");
            }
        }
        MethodDeclaration main = null;
        for (MethodDeclaration md : methods.get(binding)) {
            if (isMain(md.resolveBinding())) {
                main = md;
            }
            else {
                visit(md);
            }
        }
        code.line("}\n\n");
        for (var entry : methods.entrySet()) {
            var iface = entry.getKey();
            if (iface.equals(binding)) continue;
            code.line("impl %s for %s{\n", iface, binding);
            for (var method : entry.getValue()) {
                visit(method);
            }
            code.line("}\n\n");
        }
        if (main != null) {
            visit(main);
        }
    }

    void statics(List<VariableDeclarationFragment> node) {
        for (var frag : node) {
            FieldDeclaration fd = (FieldDeclaration) frag.getParent();
            ITypeBinding type = fd.getType().resolveBinding();
            if (this.binding.isInterface() || isStatic(fd.getModifiers(), type, frag)) {
                if (frag.getInitializer() != null) {
                    code.line("let static %s: %s = ", frag.getName(), fd.getType());
                    frag.getInitializer().accept(this);
                    code.write(";\n");
                }
                else {
                    code.line("let static %s: Option<%s> = Node;\n", frag.getName(), fd.getType());
                }
            }
        }
    }

    void methodHeader(MethodDeclaration node) {
        method = node.resolveBinding();
        if (isMain(method)) {
            code.line("pub fn main()");
            argsName = ((SingleVariableDeclaration) node.parameters().get(0)).getName().getIdentifier();
            return;
        }
        code.line("pub fn %s(", RustHelper.mapMethodName(node.resolveBinding()));
        boolean first = true;
        if (!Modifier.isStatic(node.getModifiers()) && !node.isConstructor()) {
            code.write("&self");
            first = false;
        }
        for (var param : (List<SingleVariableDeclaration>) node.parameters()) {
            if (!first) code.write(", ");
            if (param.getType().isPrimitiveType()) {
                code.write("%s: %s", param.getName().getIdentifier(), param.getType());
            }
            else {
                if (param.getType().toString().equals("String")) {
                    code.write("%s: &str", param.getName().getIdentifier());
                }
                else {
                    //all params are references
                    code.write("%s: &%s", param.getName().getIdentifier(), param.getType());
                }
            }
            first = false;
        }
        code.write(")");
        if (!node.isConstructor() && !node.getReturnType2().toString().equals("void")) {
            if (node.thrownExceptionTypes().isEmpty()) {
                code.write(" -> %s", node.getReturnType2());
            }
            else {
                code.write(" -> Result<%s, String>", node.getReturnType2());
            }
        }
    }

    public boolean visit(MethodDeclaration node) {
        methodHeader(node);
        if (node.getBody() != null) {
            node.getBody().accept(this);
        }
        else {
            code.write(";");
        }
        code.write("\n");
        return false;
    }

//    void writeDefault(TypeDeclaration node, List<VariableDeclarationFragment> fields) {
//        code.line("impl Default for %s{\n", node.getName());
//        code.line("fn default() -> Self {\n");
//        code.line("%s{}", node.getName());
//        for (var frag : fields) {
//            var fd = (FieldDeclaration) frag.getParent();
//            var type = fd.getType().resolveBinding();
//            if (isNormal(fd.getModifiers(), type, frag)) {
//
//            }
//        }
//        code.line("}");
//        code.line("}");
//    }

    public void write(String s, Object... args) {
        code.write(s, args);
    }

    @Override
    public boolean visit(Block n) {
        code.line("{\n");
        if (isMain(method)) {
            code.line("let %s: Vec<String> = env::args().collect();\n", argsName);
        }
        for (Statement s : (List<Statement>) n.statements()) {
            var m = needMultiLineMap(s);
            if (m != null) {
                for (int i = 0; i < m.size() - 1; i++) {
                    code.line("%s\n", m.get(i));
                }
            }
            s.accept(this);
            code.write("\n");
        }
        code.line("}");
        return false;
    }

    @Override
    public boolean visit(TypeDeclarationStatement node) {
        throw new RuntimeException("TypeDeclarationStatement");
    }

    @Override
    public boolean visit(AssertStatement node) {
        //throw new RuntimeException("assert");
        code.write("//%s", node.toString());
        return false;
    }

    @Override
    public boolean visit(StringLiteral node) {
        write(node.getEscapedValue());
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
        write("None");
        return true;
    }

    //type.class
    @Override
    public boolean visit(TypeLiteral node) {
        //Class::forName(type)
        code.write(node.toString());
        return true;
    }

    @Override
    public boolean visit(EmptyStatement node) {
        return false;
    }

    @Override
    public boolean visit(Assignment node) {
        node.getLeftHandSide().accept(this);
        write(node.getOperator().toString());
        node.getRightHandSide().accept(this);
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
        code.line("return");
        if (node.getExpression() != null) {
            code.write(" ");
            node.getExpression().accept(this);
        }
        code.write(";");
        return false;
    }

    @Override
    public boolean visit(ThrowStatement node) {
        code.line("throw ");
        Expression e = node.getExpression();
        e.accept(this);
        write(";");
        return false;
    }

    @Override
    public boolean visit(TryStatement node) {
        code.line("try");
        node.getBody().accept(this);
        for (var c : node.catchClauses()) {
            var cc = (CatchClause) c;
            code.line("catch(");
            cc.getException().accept(this);
            code.write(")");
            cc.getBody().accept(this);
        }
        if (node.getFinally() != null) {
            code.line("finally");
            node.getFinally().accept(this);
        }
        return false;
    }

    @Override
    public boolean visit(IfStatement node) {
        code.line("if ");
        node.getExpression().accept(this);
        if (node.getThenStatement() instanceof Block) {
            node.getThenStatement().accept(this);
        }
        else {
            code.write("{");
            node.getThenStatement().accept(this);
            code.line("}");
        }
        code.line("");
        if (node.getElseStatement() != null) {
            code.write("else ");
            if (node.getElseStatement() instanceof Block || node.getElseStatement() instanceof IfStatement) {
                node.getElseStatement().accept(this);
            }
            else {
                code.write("{\n");
                node.getElseStatement().accept(this);
                code.line("}");
            }
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
        if (node.getExpression() != null) {
            node.getExpression().accept(this);
        }
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
        var vd = node.getParameter();
        code.line("for %s in ", vd.getName());
        node.getExpression().accept(this);
        node.getBody().accept(this);
        return false;
    }

    @Override
    public boolean visit(SingleVariableDeclaration node) {
        ITypeBinding type = node.getType().resolveBinding();
        code.line("let %s: %s", node.getName(), type);
        if (node.getInitializer() != null) {
            code.write(" = ");
            node.getInitializer().accept(this);
        }
        return false;
    }

    @Override
    public boolean visit(WhileStatement node) {
        code.line("while ");
        node.getExpression().accept(this);
        node.getBody().accept(this);
        return false;
    }

    @Override
    public boolean visit(DoStatement node) {
        code.line("loop");
        code.line("{\n");
        if (node.getBody() instanceof Block block) {
            for (var st : (List<Statement>) block.statements()) {
                st.accept(this);
            }
        }
        else {
            node.getBody().accept(this);
        }
        code.line("if !");
        node.getExpression().accept(this);
        code.write("{ break; }");
        code.line("}");
        return false;
    }

    @Override
    public boolean visit(YieldStatement node) {
        throw new RuntimeException("yield");
    }

    @Override
    public boolean visit(SwitchStatement node) {
        var info = new SwitchInfo(node);
        info.make();
        code.line("match ");
        node.getExpression().accept(this);
        code.write("{");
        for (var st : (List<Statement>) node.statements()) {
            st.accept(this);
        }
        code.line("}");
        return false;
    }

    @Override
    public boolean visit(SwitchCase node) {
        if (node.isDefault()) {
            code.line("_ => {}");
        }
        else {
            boolean first = true;
            for (var e : (List<Expression>) node.expressions()) {
                if (!first) code.write(" | ");
                e.accept(this);
                first = false;
            }
            code.write(" => ");
        }
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
    public boolean visit(SynchronizedStatement node) {
        //throw new RuntimeException("synchronized");
        code.write("//%s", node);
        return false;
    }

    @Override
    public boolean visit(VariableDeclarationStatement node) {
        ITypeBinding type = node.getType().resolveBinding();
        for (VariableDeclarationFragment frag : (List<VariableDeclarationFragment>) node.fragments()) {
            if (frag.getInitializer() == null) {
                code.line("let %s: %s;", frag.getName(), type);
            }
            else {
                code.line("let %s: %s = ", frag.getName(), type);
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
                code.line("let %s: %s;", frag.getName(), type);
            }
            else {
                code.line("let %s: %s = ", frag.getName(), type);
                frag.getInitializer().accept(this);
                write(";");
            }
        }
        return false;
    }

    @Override
    public boolean visit(ConditionalExpression node) {
        code.write("if ");
        node.getExpression().accept(this);
        write(" { ");
        node.getThenExpression().accept(this);
        write(" } else { ");
        node.getElseExpression().accept(this);
        code.write(" }");
        return false;
    }

    void args(List<Expression> list) {
        for (int i = 0; i < list.size(); i++) {
            var arg = list.get(i);
            if (arg instanceof Name) {
                //probably a variable
                var type = arg.resolveTypeBinding();
                if (type != null && !type.isPrimitive()) {
                    code.write("&");
                }
            }
            arg.accept(this);
            if (i < list.size() - 1) write(", ");
        }
    }

    @Override
    public boolean visit(ThisExpression node) {
        if (node.getQualifier() == null) {
            code.write("self");
        }
        else {
            code.write("self.%s", node.getQualifier());
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
            throw new RuntimeException("SuperFieldAccess qualifier");
        }
        code.write(node.toString());
        return false;
    }

    boolean isStr(Expression e) {
        return e.resolveTypeBinding().getQualifiedName().equals("java.lang.String");
    }

    @Override
    public boolean visit(InfixExpression node) {
        String op = node.getOperator().toString();
        node.getLeftOperand().accept(this);
        if (op.equals("+") && isStr(node.getLeftOperand())) {
            code.write(".clone()");
        }
        code.write(" %s ", op);
        node.getRightOperand().accept(this);
        if (node.hasExtendedOperands()) {
            for (Expression e : (List<Expression>) node.extendedOperands()) {
                e.accept(this);
                if (op.equals("+") && isStr(e)) {
                    code.write(".clone()");
                }
                code.write(" %s ", op);
            }
        }
        return false;
    }

    @Override
    public boolean visit(PostfixExpression node) {
        node.getOperand().accept(this);
        if (node.getOperator().toString().equals("++")) {
            code.write(" += 1");
        }
        else {
            code.write(" -= 1");
        }
        return false;
    }

    @Override
    public boolean visit(PrefixExpression node) {
        write(node.getOperator().toString());
        node.getOperand().accept(this);
        return false;
    }

    @Override
    public boolean visit(ArrayAccess node) {
        node.getArray().accept(this);
        write("[");
        node.getIndex().accept(this);
        write("]");
        return false;
    }


    @Override
    public boolean visit(ArrayCreation node) {
        if (node.getInitializer() != null) {
            if (node.getType().getDimensions() == 1) {
                var init = node.getInitializer();
                var first = (Expression) init.expressions().get(0);
                var firstType = first.resolveTypeBinding();
                var type = node.getType().getElementType().resolveBinding();
                if (type.equals(firstType)) {
                    node.getInitializer().accept(this);
                }
                else {
                    //implicit cast each element
                    arrayInit(init, RustHelper.mapType(type));
                }
            }
            else {
                node.getInitializer().accept(this);
            }
        }
        else {
            //new Type[d1][d2]
            var type = node.getType().getElementType();
            makeArrayAlloc(type, node.dimensions(), 0);
        }
        return false;
    }

    void makeArrayAlloc(Type type, List<Expression> dims, int pos) {
        if (pos == dims.size()) {
            String def;
            if (type.toString().equals("boolean")) {
                def = "false";
            }
            else if (type.isPrimitiveType()) {
                def = "0";
            }
            else {
                def = "None";
            }
            code.write(def);
            code.write(")");
            return;
        }
        code.write("helper::make::<%s>(", RustHelper.makeArray(type, dims.size() - pos - 1));
        dims.get(pos).accept(this);
        code.write(", ");
        makeArrayAlloc(type, dims, pos + 1);
    }

    void arrayInit(ArrayInitializer node, String cast) {
        code.write("vec![");
        var first = true;
        for (var v : (List<Expression>) node.expressions()) {
            if (!first) code.write(", ");
            v.accept(this);
            if (cast != null) {
                code.write(" as %s", cast);
            }
            first = false;
        }
        code.write("]");
    }

    //{...}
    @Override
    public boolean visit(ArrayInitializer node) {
        arrayInit(node, null);
        return false;
    }

    @Override
    public boolean visit(InstanceofExpression node) {
        code.write("/*%s*/", node);
        return false;
    }

    @Override
    public boolean visit(CastExpression node) {
        var target = node.getType().resolveBinding();
        var exprType = node.getExpression().resolveTypeBinding();
        if (target.isPrimitive()) {
            node.getExpression().accept(this);
            code.write(" as %s", target);
            return false;
        }
        if (target.isCastCompatible(exprType)) {
            //System.out.println("static "+clazz.getType());
            //static cast
            write("(%s)", code.ptr(target));
            node.getExpression().accept(this);
        }
        else {
            //System.out.println("dynamic " + clazz.getType());
            //dynamic e.g derived class to base class
            write("dynamic_cast<%s>(", code.ptr(target));
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

    //is return value is local[move]
    boolean isReturnLocal(MethodDeclaration md) {
        if (md.isConstructor() || md.getReturnType2().toString().equals("void")) return false;
        Set<String> locals = new HashSet<>();
        for (var st : (List<Statement>) md.getBody().statements()) {
            if (st instanceof ReturnStatement ret) {
                var expr = ret.getExpression();
                if (expr instanceof ClassInstanceCreation) return true;
                if (expr instanceof SimpleName s) {
                    return locals.contains(s.getIdentifier());
                }
                if (expr instanceof MethodInvocation mi) {
//                    if (mi.resolveMethodBinding().getDeclaringClass().equals(md.resolveBinding().getDeclaringClass())) {
//                        return isReturnLocal();
//                    }
                    //todo previsit
                }
                var obj = expr.resolveConstantExpressionValue();
                return false;
            }
            else if (st instanceof VariableDeclarationStatement vd) {
                for (var frag : (List<VariableDeclarationFragment>) vd.fragments()) {
                    //todo more cases
                    if (frag.getInitializer() != null && frag.getInitializer() instanceof ClassInstanceCreation) {
                        locals.add(frag.getName().toString());
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean visit(ClassInstanceCreation node) {
        ITypeBinding binding = node.getType().resolveBinding();
        if (binding == null) {
            Logger.logBinding(this.binding, node.toString());
            throw new RuntimeException("cic null binding");
        }
        if (node.getAnonymousClassDeclaration() == null) {
            List<String> mapped = Mapper.instance.mapMethod(node.resolveConstructorBinding(), node.arguments(), null, this.binding);
            if (mapped != null) {
                code.write(mapped.get(mapped.size() - 1));
                return false;
            }
            boolean inner = node.getAnonymousClassDeclaration() != null || !Modifier.isStatic(binding.getModifiers()) && binding.isNested();
            write("%s::new(", node.getType());
            args(node.arguments());
            if (inner) {
                write(", self");
            }
            write(")");
        }
        else {
            //collect locals and set
            code.write("/*");
            code.write(node.toString());
            code.write("*/");
            //throw new RuntimeException("anony ");
        }
        return false;
    }

    @Override
    public boolean visit(ConstructorInvocation node) {
        code.line(node.toString());
        return false;
    }

    @Override
    public boolean visit(SuperConstructorInvocation node) {
        code.line(node.toString());
        return false;
    }

    @Override
    public boolean visit(SuperMethodInvocation node) {
        if (node.getQualifier() != null) {
            throw new RuntimeException("qualified SuperMethodInvocation");
        }
        code.write(node.toString());
        return false;
    }

    List<String> needMultiLineMap(Statement statement) {
        //after vardecl,while expr,for init,
        ASTNode firstLine = statement;
        if (firstLine instanceof IfStatement is) {
            firstLine = is.getExpression();
        }
        else if (firstLine instanceof WhileStatement ws) {
            firstLine = ws.getExpression();
        }
        final List<String>[] arr = new List[1];
        var visitor = new ASTVisitor() {
            @Override
            public boolean visit(IfStatement node) {
                return false;
            }

            @Override
            public boolean visit(MethodInvocation node) {
                if (node.getExpression() != null) {
                    var binding = node.resolveMethodBinding();
                    List<String> a = Mapper.instance.mapMethod(binding, node.arguments(), node.getExpression(), Rust.this.binding);
                    if (a != null && a.size() > 1) {
                        arr[0] = a;
                    }
                }
                return false;
            }
        };
        firstLine.accept(visitor);
        return arr[0];
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean visit(MethodInvocation node) {
        IMethodBinding binding = node.resolveMethodBinding();
        if (binding == null) {
            Logger.logBinding(this.binding, node.toString());
            return visitMI(node);
            //throw new RuntimeException("inv null binding");
        }
        if (node.getExpression() != null) {
            List<String> res = Mapper.instance.mapMethod(binding, node.arguments(), node.getExpression(), this.binding);
            if (res != null) {
                code.write(res.get(res.size() - 1));
                return false;
            }
        }
        String name = RustHelper.mapMethodName(binding);
        if (Modifier.isStatic(binding.getModifiers())) {
            ITypeBinding onType = binding.getDeclaringClass();
            code.write("%s::%s(", onType, name);
            args(node.arguments());
            code.write(")");
            return false;
        }
        if (node.getExpression() != null) {
            node.getExpression().accept(this);
            write(".");
        }
        write(name);
        write("(");
        args(node.arguments());
        write(")");
        return false;
    }


    @SuppressWarnings("unchecked")
    public boolean visitMI(MethodInvocation node) {
        if (node.getExpression() != null) {
            node.getExpression().accept(this);
            code.write(".");
        }
        code.write(node.getName().toString());
        code.write("(");
        args(node.arguments());
        code.write(")");
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
        var name = RustHelper.map(node.getIdentifier());
        if (binding == null) {
            Logger.logBinding(this.binding, node.toString());
            code.write(node.getIdentifier());
            return false;
//            throw new RuntimeException("sn null binding");
        }
        if (binding.getKind() == IBinding.TYPE) {
            code.write(RustHelper.toRustType(name));
            return false;
        }
        if (binding.getKind() != IBinding.VARIABLE) {
            code.write(name);
            throw new RuntimeException("non var sn " + node);
        }

        IVariableBinding variableBinding = (IVariableBinding) binding;
        if (variableBinding.isParameter()) {
            write(Mapper.instance.mapParamName(node.getIdentifier()));
            return false;
        }
        ITypeBinding onType = variableBinding.getDeclaringClass();

        if (Modifier.isStatic(binding.getModifiers())) {
            //still works bc onType represents file name not struct
            write("%s::%s", onType, name);
            return false;
        }
        if (this.binding.equals(onType)) {
            //local field
            code.write("self.%s", name);
            return false;
        }
        if (this.binding.isSubTypeCompatible(onType)) {
            //super field
            code.write("self.super_.%s", name);
            return false;
        }
        code.write(name);
        return false;
    }

    //qualified class,array.length,field access
    @Override
    public boolean visit(QualifiedName node) {
        IBinding binding = node.resolveBinding();
        ITypeBinding onType = node.getQualifier().resolveTypeBinding();
        var name = RustHelper.map(node.getName().getIdentifier());

        if ((binding instanceof IVariableBinding) && Config.writeLibHeader) {
            LibHandler.instance.addField((IVariableBinding) binding);
        }
        if (binding == null || onType == null) {
            node.getQualifier().accept(this);
            code.write(".");
            code.write(name);
            Logger.logBinding(this.binding, node.toString());
            //normal qualified name access
            //qualifier is not a type so it is a package
            //return new CName(node.getFullyQualifiedName());
            //throw new RuntimeException("qname null binding");
            return false;
        }
        if (onType.isArray() && name.equals("length")) {
            //array.length
            node.getQualifier().accept(this);
            code.write(".len()");
            return false;
        }

        boolean isStatic = Modifier.isStatic(binding.getModifiers());

        if (isStatic) {
            write("%s::%s", onType, name);
            return false;
        }

        if (binding instanceof IVariableBinding) {
            var vb = (IVariableBinding) binding;
            if (vb.isParameter()) {
                code.write(name);
            }
        }
        else {
            code.write("self.");
            node.getQualifier().accept(this);
            code.write(".");
            code.write(name);
        }
        return false;
    }

    @Override
    public boolean visit(FieldAccess node) {
        IVariableBinding binding = node.resolveFieldBinding();
        Expression scope = node.getExpression();
        ITypeBinding typeBinding = scope.resolveTypeBinding();
        if (typeBinding == null) {
            Logger.logBinding(this.binding, node.toString());
            throw new RuntimeException("null type binding");
        }
        else if (typeBinding.isArray() && node.getName().getIdentifier().equals("length")) {
            scope.accept(this);
            write("->len()");
            return false;
        }

        if (binding.isEnumConstant() || Modifier.isStatic(binding.getModifiers())) {
            scope.accept(this);
            write("::%s", node.getName());
            return false;
        }

        scope.accept(this);
        write("->%s", node.getName());
        //todo Main.this.field
        return false;
    }
}