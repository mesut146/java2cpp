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

    public DeclarationVisitor(SourceVisitor sourceVisitor, TypeVisitor typeVisitor) {
        this.sourceVisitor = sourceVisitor;
        this.typeVisitor = typeVisitor;
        this.header = sourceVisitor.getHeader();
    }

    public CClass visit(TypeDeclaration node, CClass clazz) {
        //System.out.println("type.decl=" + node.getName());
        CClass cc = new CClass();
        if (clazz != null) {
            clazz.addInner(cc);
        }

        cc.name = node.getName().getFullyQualifiedName();
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
        //System.out.println("enum.decl=" + n.getName());
        CClass cc = new CClass();
        if (clazz != null) {
            clazz.addInner(cc);
        }


        cc.name = n.getName().getFullyQualifiedName();
        cc.base.add(Helper.getEnumType().setHeader(header));
        header.addInclude("java/lang/Enum");

        n.superInterfaceTypes().forEach(iface -> cc.base.add(typeVisitor.visit((Type) iface)));

        for (EnumConstantDeclaration constant : (List<EnumConstantDeclaration>) n.enumConstants()) {
            CField field = new CField();
            cc.addField(field);
            field.setPublic(true);
            field.setStatic(true);
            field.type = new CType(cc.name, header);
            field.setName(constant.getName().getIdentifier());

            if (!constant.arguments().isEmpty()) {
                CClassInstanceCreation args = new CClassInstanceCreation();
                args.type = field.type;
                for (Expression val : (List<Expression>) constant.arguments()) {
                    args.args.add((CExpression) sourceVisitor.visitExpr(val, null));
                }
                field.expression = args;
            }
            else {
                //todo make ordinals
                //throw new RuntimeException("ordinal");
            }

            if (constant.getAnonymousClassDeclaration() != null) {
                throw new RuntimeException("anonymous enum constant is not supported");
            }
        }
        n.bodyDeclarations().forEach(body -> visitBody((BodyDeclaration) body, cc));
        //todo put values and valueOf method
        return cc;
    }

    public void visit(FieldDeclaration n, CClass clazz) {
        CType type = typeVisitor.visitType(n.getType());
        //System.out.println("field.decl=" + n.getType() + " " + n.fragments());
        for (VariableDeclarationFragment frag : (List<VariableDeclarationFragment>) n.fragments()) {
            CField field = new CField();
            clazz.addField(field);
            field.type = type;
            field.setName(frag.getName().getIdentifier());
            field.setPublic(Modifier.isPublic(n.getModifiers()));
            field.setStatic(Modifier.isStatic(n.getModifiers()));
            field.setProtected(Modifier.isProtected(n.getModifiers()));
            if (clazz.isInterface) {
                field.setPublic(true);
            }
            if (frag.getInitializer() != null) {
                SourceVisitor visitor = new SourceVisitor(sourceVisitor.converter, header.source);
                visitor.clazz = clazz;
                field.expression = (CExpression) visitor.visitExpr(frag.getInitializer(), null);
            }
        }
    }


    public CNode visit(MethodDeclaration node, CClass clazz) {
        CMethod methodDecl = new CMethod();

        node.typeParameters().forEach(temp -> {
            methodDecl.template.add(new CType(temp.toString()).setHeader(sourceVisitor.source));
        });

        if (node.isConstructor()) {
            methodDecl.isCons = true;
        }
        else {
            Type type = node.getReturnType2();
            if (type == null) {
                methodDecl.isCons = true;
            }
            else {
                methodDecl.type = typeVisitor.visitType(node.getReturnType2());
            }
        }

        //type could be template
        methodDecl.name = new CName(node.getName().getIdentifier());

        methodDecl.setStatic(Modifier.isStatic(node.getModifiers()));
        methodDecl.setPublic(Modifier.isPublic(node.getModifiers()));
        methodDecl.setNative(Modifier.isNative(node.getModifiers()));
        if (clazz.isInterface) {
            methodDecl.setPublic(true);
            methodDecl.isPureVirtual = true;
        }

        for (SingleVariableDeclaration param : (List<SingleVariableDeclaration>) node.parameters()) {
            CParameter cp = new CParameter();
            cp.type = typeVisitor.visitType(param.getType());
            cp.type.isTemplate = false;
            cp.setName(param.getName().getIdentifier());
            methodDecl.params.add(cp);
        }

        methodDecl.body = (CBlockStatement) sourceVisitor.visit(node.getBody(), null);
        return methodDecl;
    }
}
