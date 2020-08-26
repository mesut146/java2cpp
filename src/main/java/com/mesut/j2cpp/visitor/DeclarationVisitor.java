package com.mesut.j2cpp.visitor;

import com.mesut.j2cpp.ast.*;
import com.mesut.j2cpp.cppast.CExpression;
import com.mesut.j2cpp.cppast.CNode;
import com.mesut.j2cpp.cppast.expr.CClassInstanceCreation;
import com.mesut.j2cpp.cppast.stmt.CBlockStatement;
import com.mesut.j2cpp.util.Helper;
import org.eclipse.jdt.core.dom.*;

import java.util.List;

@SuppressWarnings("unchecked")
public class DeclarationVisitor {

    SourceVisitor sourceVisitor;
    TypeVisitor typeVisitor;
    public CHeader header;

    public DeclarationVisitor(SourceVisitor sourceVisitor) {
        this.sourceVisitor = sourceVisitor;
        this.typeVisitor = sourceVisitor.getTypeVisitor();
        this.header = sourceVisitor.getHeader();
    }


    public void convert(CompilationUnit cu) {
        visit(cu.getPackage());
        for (AbstractTypeDeclaration decl : (List<AbstractTypeDeclaration>) cu.types()) {
            if (decl instanceof EnumDeclaration) {
                visit((EnumDeclaration) decl, null);
            }
            else {
                visit((TypeDeclaration) decl, null);
            }
        }
    }


    public void visit(PackageDeclaration n) {
        Namespace ns = new Namespace();
        ns.fromPkg(n.getName().getFullyQualifiedName());
        header.ns = ns;
        header.using.add(header.ns);
        header.source.usings.add(header.ns);
    }


    public CClass visit(TypeDeclaration node, CClass clazz) {
        //System.out.println("type.decl=" + node.getName());
        CClass cc = new CClass();
        if (clazz != null) {
            clazz.addInner(cc);
        }
        else {
            header.addClass(cc);
        }

        cc.name = node.getName().getFullyQualifiedName();
        cc.getType().forward();
        cc.isInterface = node.isInterface();

        node.typeParameters().forEach(type -> cc.template.add(new CType(type.toString(), true).setHeader(header)));

        if (node.getSuperclassType() != null) {
            CType baseType = typeVisitor.visitType(node.getSuperclassType());
            baseType.isTemplate = false;
            baseType.isPointer = false;
            cc.base.add(baseType);
        }

        node.superInterfaceTypes().forEach(iface -> {
            CType ifType = typeVisitor.visitType((Type) iface);
            ifType.isTemplate = false;
            ifType.isPointer = false;
            cc.base.add(ifType);
        });

        node.bodyDeclarations().forEach(body -> visitBody((BodyDeclaration) body, cc));
        return cc;
    }

    private void visitBody(BodyDeclaration body, CClass cc) {
        if (body instanceof EnumDeclaration) {
            visit((EnumDeclaration) body, cc);
        }
        else if (body instanceof FieldDeclaration) {
            visit((FieldDeclaration) body, cc);
        }
        else if (body instanceof MethodDeclaration) {
            visit((MethodDeclaration) body, cc);
        }
        else if (body instanceof TypeDeclaration) {//inner class
            visit((TypeDeclaration) body, cc);
        }
    }

    public CClass visit(EnumDeclaration n, CClass clazz) {
        CClass cc = new CClass();
        if (clazz != null) {
            clazz.addInner(cc);
        }
        else {
            header.addClass(cc);
        }

        cc.name = n.getName().getFullyQualifiedName();
        cc.base.add(Helper.getEnumType().setHeader(header));
        cc.getType().forward();
        header.addInclude("java/lang/Enum");

        n.superInterfaceTypes().forEach(iface -> cc.base.add(typeVisitor.visit((Type) iface)));

        for (EnumConstantDeclaration constant : (List<EnumConstantDeclaration>) n.enumConstants()) {
            CField field = new CField();
            cc.addField(field);
            field.setPublic(true);
            field.setStatic(true);
            field.setType(new CType(cc.name, header));
            field.setName(constant.getName().getIdentifier());

            if (!constant.arguments().isEmpty()) {
                CClassInstanceCreation args = new CClassInstanceCreation();
                args.setType(field.type);
                for (Expression val : (List<Expression>) constant.arguments()) {
                    args.args.add((CExpression) sourceVisitor.visitExpr(val, null));
                }
                field.expression = args;
            }
            else {
                //todo make ordinals
                CClassInstanceCreation args = new CClassInstanceCreation();
                args.setType(field.type);
                field.expression = args;
            }

            if (constant.getAnonymousClassDeclaration() != null) {
                throw new RuntimeException("anonymous enum constant is not supported");
            }
        }
        if (!n.bodyDeclarations().isEmpty()) {
            n.bodyDeclarations().forEach(p -> visitBody((BodyDeclaration) p, cc));
        }
        //todo put values and valueOf method
        return cc;
    }

    public void visit(FieldDeclaration n, CClass clazz) {
        CType type = typeVisitor.visitType(n.getType());

        for (VariableDeclarationFragment frag : (List<VariableDeclarationFragment>) n.fragments()) {
            CField field = new CField();
            clazz.addField(field);
            field.setType(type);
            field.setName(frag.getName().getIdentifier());
            field.setPublic(Modifier.isPublic(n.getModifiers()));
            field.setStatic(Modifier.isStatic(n.getModifiers()));
            field.setProtected(Modifier.isProtected(n.getModifiers()));
            if (clazz.isInterface) {
                field.setPublic(true);
            }
            if (frag.getInitializer() != null) {
                sourceVisitor.clazz = clazz;
                field.expression = (CExpression) sourceVisitor.visitExpr(frag.getInitializer(), null);
                sourceVisitor.clazz = null;
            }
        }
    }


    public CNode visit(MethodDeclaration node, CClass clazz) {
        CMethod method = new CMethod();
        clazz.addMethod(method);

        node.typeParameters().forEach(temp -> method.template.add(new CType(temp.toString(), true)));

        if (node.isConstructor()) {
            method.isCons = true;
        }
        else {
            Type type = node.getReturnType2();
            method.setType(typeVisitor.visitType(type));
        }

        method.name = new CName(node.getName().getIdentifier());

        method.setStatic(Modifier.isStatic(node.getModifiers()));
        method.setPublic(Modifier.isPublic(node.getModifiers()));
        method.setNative(Modifier.isNative(node.getModifiers()));
        if (clazz.isInterface || Modifier.isAbstract(node.getModifiers())) {
            method.setPublic(true);
            method.setVirtual(true);
            method.isPureVirtual = true;
        }

        for (SingleVariableDeclaration param : (List<SingleVariableDeclaration>) node.parameters()) {
            CParameter cp = new CParameter();
            cp.method = method;
            cp.setType(typeVisitor.visitType(param.getType()));

            cp.setName(param.getName().getIdentifier());
            method.addParam(cp);
        }

        sourceVisitor.clazz = clazz;
        sourceVisitor.method = method;
        method.body = (CBlockStatement) sourceVisitor.visitExpr(node.getBody(), null);
        return method;
    }
}
