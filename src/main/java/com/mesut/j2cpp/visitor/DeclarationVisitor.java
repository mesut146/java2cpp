package com.mesut.j2cpp.visitor;

import com.mesut.j2cpp.Config;
import com.mesut.j2cpp.ast.*;
import com.mesut.j2cpp.cppast.CExpression;
import com.mesut.j2cpp.cppast.CNode;
import com.mesut.j2cpp.cppast.expr.CAssignment;
import com.mesut.j2cpp.cppast.expr.CClassInstanceCreation;
import com.mesut.j2cpp.cppast.expr.CFieldAccess;
import com.mesut.j2cpp.cppast.expr.CThisExpression;
import com.mesut.j2cpp.cppast.stmt.CBlockStatement;
import com.mesut.j2cpp.cppast.stmt.CExpressionStatement;
import com.mesut.j2cpp.util.TypeHelper;
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
            else if (decl instanceof AnnotationTypeDeclaration) {
                visit((AnnotationTypeDeclaration) decl, null);
            }
            else {
                visit((TypeDeclaration) decl, null);
            }
        }
    }

    public void visit(PackageDeclaration n) {
        Namespace ns = new Namespace();
        if (n != null) {
            ns.fromPkg(n.getName().getFullyQualifiedName());
        }
        header.ns = ns;
        header.using.add(header.ns);
        header.source.usings.add(header.ns);
    }

    public CClass visit(AnnotationTypeDeclaration node, CClass clazz) {
        CClass cc = new CClass();
        if (clazz != null) {
            clazz.addInner(cc);
        }
        else {
            header.addClass(cc);
        }
        cc.isInterface = true;
        cc.base.add(new CType("java::lang::Annotation"));
        cc.name = node.getName().getIdentifier();

        node.bodyDeclarations().forEach(body -> visitBody((BodyDeclaration) body, cc));
        return cc;
    }

    public CClass visit(TypeDeclaration node, CClass clazz) {
        CClass cc = new CClass();

        if (!Config.move_inners && clazz != null) {
            clazz.addInner(cc);
        }
        else {
            header.addClass(cc);
        }

        cc.name = node.getName().getFullyQualifiedName();
        cc.isInterface = node.isInterface();
        cc.getType().forward(header);

        node.typeParameters().forEach(type -> cc.template.add(new CType(type.toString(), true)));

        if (node.getSuperclassType() != null) {
            CType baseType = typeVisitor.visitType(node.getSuperclassType(), cc);
            baseType.isTemplate = false;
            baseType.isPointer = false;
            cc.base.add(baseType);
        }

        node.superInterfaceTypes().forEach(iface -> {
            CType ifType = typeVisitor.visitType((Type) iface,cc);
            ifType.isTemplate = false;
            ifType.isPointer = false;
            cc.base.add(ifType);
        });

        node.bodyDeclarations().forEach(body -> visitBody((BodyDeclaration) body, cc));

        //handle inner's parent reference
        if (clazz != null) {
            CName this_parent = CName.from("_this_parent");
            //make constructor for parent reference
            CField field = new CField();
            field.setType(clazz.getType());
            field.setName(this_parent);
            cc.addField(field);
            //make or get constructor for init field
            CMethod cons = cc.getMethod(true, null, clazz.getType());
            if (cons == null) {
                cons = new CMethod();
                cons.isCons = true;
                cons.name = CName.from(cc.name);
                cons.body = new CBlockStatement();
                cc.addMethod(cons);
            }
            CParameter parameter = new CParameter();
            parameter.setType(clazz.getType());
            parameter.setName(this_parent);
            cons.addParam(parameter);
            CFieldAccess access = new CFieldAccess();
            access.isArrow = true;
            access.scope = new CThisExpression();
            access.name = this_parent;
            cons.body.addStatement(new CExpressionStatement(new CAssignment(access, this_parent, "=")));
            //replace references with scoped references
        }
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
        if (!Config.move_inners && clazz != null) {
            clazz.addInner(cc);
        }
        else {
            header.addClass(cc);
        }

        cc.name = n.getName().getFullyQualifiedName();
        cc.base.add(TypeHelper.getEnumType());
        cc.getType().forward(header);
        header.addInclude("java/lang/Enum");

        n.superInterfaceTypes().forEach(iface -> cc.base.add(typeVisitor.visitType((Type) iface,cc)));

        for (EnumConstantDeclaration constant : (List<EnumConstantDeclaration>) n.enumConstants()) {
            CField field = new CField();
            cc.addField(field);
            field.setPublic(true);
            field.setStatic(true);
            field.setType(new CType(cc.name));
            field.setName(constant.getName().getIdentifier());

            CClassInstanceCreation rhs = new CClassInstanceCreation();
            field.expression = rhs;
            rhs.setType(field.type);

            if (!constant.arguments().isEmpty()) {
                for (Expression val : (List<Expression>) constant.arguments()) {
                    rhs.args.add((CExpression) sourceVisitor.visitExpr(val, null));
                }
            }

            if (constant.getAnonymousClassDeclaration() != null) {
                CClassInstanceCreation creation = AnonyHandler.handle(constant.getAnonymousClassDeclaration(), cc.getType(), cc, sourceVisitor);
                rhs.setType(creation.type);
            }
        }
        if (!n.bodyDeclarations().isEmpty()) {
            n.bodyDeclarations().forEach(p -> visitBody((BodyDeclaration) p, cc));
        }
        //todo put values and valueOf method
        return cc;
    }

    public void visit(FieldDeclaration n, CClass clazz) {
        CType type = typeVisitor.visitType(n.getType(),clazz);

        for (VariableDeclarationFragment frag : (List<VariableDeclarationFragment>) n.fragments()) {
            CField field = new CField();
            clazz.addField(field);
            if (frag.getExtraDimensions() > 0) {
                //c style array
                CArrayType arrayType = new CArrayType(type.copy(), frag.getExtraDimensions());
                field.setType(arrayType);
            }
            else {
                field.setType(type);
            }

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
            method.setType(typeVisitor.visitType(type,clazz));
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
            CType ptype = typeVisitor.visitType(param.getType(),clazz);


            if (param.isVarargs()) {//todo not just array, maybe as vararg
                cp.setType(new CArrayType(ptype, 1));
            }
            else {
                if (param.getExtraDimensions() > 0) {
                    CArrayType arrayType = new CArrayType(ptype, param.getExtraDimensions());
                    cp.setType(arrayType);
                }
                else {
                    cp.setType(ptype);
                }
            }

            cp.setName(param.getName().getIdentifier());
            method.addParam(cp);
        }

        sourceVisitor.clazz = clazz;
        sourceVisitor.method = method;
        method.body = (CBlockStatement) sourceVisitor.visitExpr(node.getBody(), null);
        return method;
    }
}
