package com.mesut.j2cpp.visitor;

import com.mesut.j2cpp.Config;
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
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class SourceVisitor2 extends ASTVisitor {

    Path dir;
    CompilationUnit cu;
    ITypeBinding binding;
    String catchName;
    Code code = new Code();
    int forVarCnt = 0;

    public SourceVisitor2(Path dir, CompilationUnit cu) {
        this.dir = dir;
        this.cu = cu;
    }

    public void all(Path rel) {
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
            Files.write(file, code.toString().getBytes());
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
        code.write("%s %s::%s(", code.ptr(b.getReturnType()), this.binding, b.getName());
        for (int i = 0; i < node.parameters().size(); i++) {
            SingleVariableDeclaration param = (SingleVariableDeclaration) node.parameters().get(i);
            code.write("%s %s", code.ptr(param.getType().resolveBinding()), Mapper.instance.mapParamName(param.getName().getIdentifier()));
            if (i < node.parameters().size() - 1) code.write(", ");
        }
        code.write(")");
        node.getBody().accept(this);
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
            s.accept(this);
            code.write("\n");
        }
        code.down();
        code.line("}");
        return false;
    }

    @Override
    public boolean visit(TypeDeclarationStatement node) {
        /*TypeDeclaration declaration = (TypeDeclaration) node.getDeclaration();
        DeclarationVisitor visitor = new DeclarationVisitor(this);
        visitor.visit(declaration, null);*/
        throw new RuntimeException("TypeDeclarationStatement");
    }

    @Override
    public boolean visit(AssertStatement node) {
        throw new RuntimeException("assert");
    }

    @Override
    public boolean visit(StringLiteral node) {
        write("new %s(%s)", TypeHelper.getStringType(), node.getEscapedValue());
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
        code.line("return");
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
                throw new RuntimeException("throw other");
            }
        write(";");
        return false;
    }

    @Override
    public boolean visit(TryStatement node) {
        throw new RuntimeException("try");
        //TryHelper helper = new TryHelper(this, node);
        //helper.handle();
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
            throw new RuntimeException("foreach array");
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
        throw new RuntimeException("yield");
    }

    @Override
    public boolean visit(SwitchStatement node) {
        /*Expression expression = node.getExpression();
        ITypeBinding binding = expression.resolveTypeBinding();
        SwitchHelper helper = new SwitchHelper(this);
        if (binding.isEnum()) {
            //if else with ordinals
            helper.isEnum = true;
        } else if (binding.getQualifiedName().equals("java.lang.String")) {
            helper.isString = true;
        } else if (binding.isPrimitive()) {
            helper.makeNormal(node);
        }
        List < CStatement > list = helper.makeIfElse(node);
        new CMultiStatement(list);*/
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
        /*Call thisCall = new Call();
        for (Expression ar: (List < Expression > ) node.arguments()) {
            thisCall.args.add((CExpression) visitExpr(ar, arg));
        }
        thisCall.isThis = true;
        thisCall.type = clazz.getType();
        method.thisCall = thisCall;*/
        return false;
    }

    @Override
    public boolean visit(SuperConstructorInvocation node) {
        /*Call superCall = new Call();
        superCall.isThis = false;
        superCall.args = list(node.arguments());
        superCall.type = clazz.superClass;
        method.superCall = superCall;*/
        return false;
    }

    @Override
    public boolean visit(SynchronizedStatement node) {
        throw new RuntimeException("synchronized");
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
            throw new RuntimeException("null type binding");
        }
        else if (typeBinding.isArray() && node.getName().getIdentifier().equals("length")) {
            scope.accept(this);
            write("->size()");
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

    @Override
    public boolean visit(ClassInstanceCreation node) {
        ITypeBinding binding = node.getType().resolveBinding();
        if (binding == null) {
            Logger.logBinding(this.binding, node.toString());
            throw new RuntimeException("cic null binding");
        }
        boolean inner = node.getAnonymousClassDeclaration() != null || !Modifier.isStatic(binding.getModifiers()) && binding.isNested();
        if (node.getAnonymousClassDeclaration() == null) {
            write("new %s(", node.getType().resolveBinding());
            args(node.arguments());
            if (inner) {
                write(", this");
            }
            write(")");
        }
        else {
            //collect locals and set
            throw new RuntimeException("anony ");
                /*AnonyHandler handler = new AnonyHandler();
                creation = handler.handle(node.getAnonymousClassDeclaration(), TypeVisitor.visitType(node.getType(), clazz), clazz, this);
                anony = handler.anony;
                */
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
            throw new RuntimeException("qualified SuperMethodInvocation");
        }
        code.write("%s::%s(", binding.getSuperclass(), node.getName());
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
                throw new RuntimeException("qualified this");
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
            throw new RuntimeException("SuperFieldAccess qualifier");
        }
        code.write("%s::%s", binding.getSuperclass(), node.getName());
        return false;
    }

    void wrapStr(Expression e) {
        code.write("%s::valueOf(", TypeHelper.getStringType());
        e.accept(this);
        code.write(")");
    }

    @Override
    public boolean visit(InfixExpression node) {
        String op = node.getOperator().toString();
        if (op.equals("+")) {
            //if either side is string then whole expr is string concatenation
            if (isStr(node.getLeftOperand())) {
                node.getLeftOperand().accept(this);
                code.write("->concat(");
                node.getRightOperand().accept(this);
                code.write(")");
                if (node.hasExtendedOperands()) {
                    throw new RuntimeException("str concat");
                }
            }
            else if (isStr(node.getRightOperand())) {
                wrapStr(node.getLeftOperand());
                code.write("->concat(");
                node.getRightOperand().accept(this);
                code.write(")");
                if (node.hasExtendedOperands()) {
                    throw new RuntimeException("str concat");
                }
            }
        }
        if (node.getOperator().toString().equals(">>>")) {
            CType type = TypeVisitor.fromBinding(node.getLeftOperand().resolveTypeBinding());
            write("(unsigned %s)", type);
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
        return e.resolveTypeBinding().getQualifiedName().equals("java.lang.String");
    }

        /*CExpression infixString(InfixExpression node) {
            CClassInstanceCreation creation = new CClassInstanceCreation();
            CInfixExpression res = new CInfixExpression();
            res.operator = "+";
            Expression left = node.getLeftOperand();
            Expression right = node.getRightOperand();
            res.left = makeStrIfNot(left);
            res.right = makeStrIfNot(right);
            if ((left instanceof StringLiteral || left instanceof CharacterLiteral) && (right instanceof StringLiteral || right instanceof CharacterLiteral)) {
                //one must be converted
                CMethodInvocation invocation = new CMethodInvocation();
                invocation.name = CName.simple("std::string");
                invocation.arguments.add(res.left);
                res.left = invocation;
            }
            if (node.hasExtendedOperands()) {
                for (Expression expression: (List < Expression > ) node.extendedOperands()) {
                    res.other.add(makeStrIfNot(expression));
                }
            }
            creation.args.add(res);
            creation.type = Mapper.instance.mapType(TypeHelper.getStringType(), clazz);
            return creation;
        }*/

        /*CExpression makeStrIfNot(Expression node) {
            if (node instanceof StringLiteral) {
                StringLiteral lit = (StringLiteral) node;
                return new CStringLiteral(lit.getLiteralValue(), lit.getEscapedValue());
            } else {
                ITypeBinding binding = node.resolveTypeBinding();
                CExpression expression = (CExpression) visitExpr(node, null);
                if (binding.isPrimitive()) {
                    if (node instanceof CharacterLiteral) {
                        return new CStringLiteral("" + ((CharacterLiteral) node).charValue());
                    }
                    CMethodInvocation invocation = new CMethodInvocation();
                    invocation.name = CName.simple("std::to_string");
                    invocation.arguments.add(expression);
                    return invocation;
                } else {
                    if (binding.getQualifiedName().equals("java.lang.String")) {
                        return new DeferenceExpr(expression);
                    } else {
                        //may have toString
                        for (IMethodBinding methodBinding: binding.getDeclaredMethods()) {
                            if (methodBinding.getName().equals("toString") && methodBinding.getReturnType().getQualifiedName().equals("java.lang.String")) {
                                CMethodInvocation invocation = new CMethodInvocation();
                                invocation.isArrow = true;
                                invocation.scope = expression;
                                invocation.name = new CName("toString");
                                return new DeferenceExpr(invocation);
                            }
                        }
                        //leave as it is
                        return expression;
                    }
                }
            }
        }*/

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
        /*
        //source.hasRuntime = true;
        write("instance_of<");
        write(node.getRightOperand().resolveBinding());
        write(">(");
        node.getLeftOperand().accept(this);
        write(")");*/
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
        throw new RuntimeException("ArrayInitializer");
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
            throw new RuntimeException("inv null binding");
        }
        ITypeBinding ret = binding.getReturnType();
        ITypeBinding org = binding.getMethodDeclaration().getReturnType();
        boolean needCast = !org.equals(ret) && (org.isWildcardType() || org.isTypeVariable());
        if (needCast) {
            //cast
            write("dynamic_cast<%s>(", code.ptr(ret));
        }
        System.out.printf("rt of %s = %s other=%s\n", node, ret.getName(), org.getName());
        boolean isStatic = Modifier.isStatic(binding.getModifiers());
        if (node.getExpression() == null) {
            ITypeBinding onType = binding.getDeclaringClass();
            //CType type = TypeVisitor.fromBinding(onType, cla);
            if (isStatic) {
                //static method needs qualifier
                code.write(onType);
                write("::");
            }
            else if (Config.qualifyBaseMethod && !this.binding.equals(onType) && this.binding.isSubTypeCompatible(onType) && !onType.isInterface()) {
                //qualify super method,not needed but more precise
                code.write(onType);
                write("::");
            }
            write(node.getName().toString());
            write("(");
            args(node.arguments());
            write(")");
        }
        else {
            node.getExpression().accept(this);
            if (catchName != null && catchName.equals(node.getExpression().toString())) {
                write(".");
            }
            else if (isStatic) {
                write("::");
            }
            else {
                write("->");
            }
            write(node.getName().toString());
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
            throw new RuntimeException("sn null binding");
        }
        if (binding.getKind() != IBinding.VARIABLE) {
            write(name.toString());
            throw new RuntimeException("non var sn " + node);
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
                throw new RuntimeException("sn field");
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
        }
        if (binding == null || typeBinding == null) {
            Logger.logBinding(this.binding, node.toString());
            //normal qualified name access
            //qualifier is not a type so it is a package
            //return new CName(node.getFullyQualifiedName());
            throw new RuntimeException("qname null binding");
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
            if (Config.static_field_cofui && Modifier.isFinal(binding.getModifiers()) && !variableBinding.getType().isPrimitive()) {
                CName mapped = Mapper.instance.mapFieldName(name, this.binding);
                write("%s::%s()", typeBinding, mapped);
                return false;
            }
        }

        if (binding.getKind() == IBinding.VARIABLE) {
            IVariableBinding variableBinding = (IVariableBinding) binding;
            if (variableBinding.isField()) {
                CName mapped = Mapper.instance.mapFieldName(name, this.binding);
                write("%s->%s", typeBinding, mapped);
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
			throw new RuntimeException("infix box");
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