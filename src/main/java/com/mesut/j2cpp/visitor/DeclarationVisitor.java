package com.mesut.j2cpp.visitor;

import com.mesut.j2cpp.Config;
import com.mesut.j2cpp.Logger;
import com.mesut.j2cpp.ast.*;
import com.mesut.j2cpp.cppast.CExpression;
import com.mesut.j2cpp.cppast.CNode;
import com.mesut.j2cpp.cppast.expr.CClassInstanceCreation;
import com.mesut.j2cpp.cppast.stmt.CBlockStatement;
import com.mesut.j2cpp.map.ClassMap;
import com.mesut.j2cpp.util.ArrayHelper;
import com.mesut.j2cpp.util.DepVisitor;
import com.mesut.j2cpp.util.TypeHelper;
import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public class DeclarationVisitor {

    public boolean visitSource = true;
    public List<CClass> classes = new ArrayList<>();
    public Namespace ns;
    SourceVisitor sourceVisitor;

    public DeclarationVisitor(SourceVisitor sourceVisitor) {
        this.sourceVisitor = sourceVisitor;
    }

    public void convert(CompilationUnit cu) {
        ns = visit(cu.getPackage());

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

    public Namespace visit(PackageDeclaration n) {
        Namespace ns = new Namespace();
        if (n != null) {
            ns = new Namespace(n.getName().getFullyQualifiedName());
        }
        return ns;
    }

    public CClass visit(AnnotationTypeDeclaration node, CClass outer) {
        CClass cc = new CClass();
        classes.add(cc);
        cc.isInterface = true;
        cc.addBase(TypeHelper.getAnnotationType());
        cc.name = node.getName().getIdentifier();
        node.bodyDeclarations().forEach(body -> visitBody((BodyDeclaration) body, cc));
        return cc;
    }

    public CClass visit(TypeDeclaration node, CClass outer) {
        CClass cc = ClassMap.sourceMap.get(TypeVisitor.fromBinding(node.resolveBinding()));
        classes.add(cc);
        node.bodyDeclarations().forEach(body -> visitBody((BodyDeclaration) body, cc));
        //new DepVisitor(cc, node).handle();
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
        else if (body instanceof Initializer) {
            visit((Initializer) body, cc);
        }
        else {
            System.out.println("body type: " + body.getClass());
        }
    }

    public CClass visit(EnumDeclaration n, CClass outer) {
        CClass cc = ClassMap.sourceMap.get(TypeVisitor.fromBinding(n.resolveBinding()));
        classes.add(cc);

        for (EnumConstantDeclaration constant : (List<EnumConstantDeclaration>) n.enumConstants()) {
            CField field = PreVisitor.visitField(constant.resolveVariable(), cc);

            CClassInstanceCreation rhs = new CClassInstanceCreation();
            field.expression = rhs;
            rhs.setType(field.type);

            if (!constant.arguments().isEmpty()) {
                rhs.args = sourceVisitor.list(constant.arguments());
            }
            if (constant.getAnonymousClassDeclaration() != null) {
                CClassInstanceCreation creation = AnonyHandler.handle(constant.getAnonymousClassDeclaration(), cc.getType(), cc, sourceVisitor);
                rhs.setType(creation.type);
            }
        }
        if (!n.bodyDeclarations().isEmpty()) {
            n.bodyDeclarations().forEach(p -> visitBody((BodyDeclaration) p, cc));
        }
        return cc;
    }

    public void visit(FieldDeclaration n, CClass clazz) {
        CType type = TypeVisitor.fromBinding(n.getType().resolveBinding(), clazz);

        for (VariableDeclarationFragment frag : (List<VariableDeclarationFragment>) n.fragments()) {
            CField field = PreVisitor.visitField(frag.resolveBinding(), clazz);
            if (clazz.isInterface) {
                field.setStatic(true);
            }
            if (frag.getExtraDimensions() > 0) {
                //c style array
                field.setType(ArrayHelper.makeArrayType(type, frag.getExtraDimensions()));
            }
            else {
                field.setType(type);
            }

            if (frag.getInitializer() != null) {
                sourceVisitor.clazz = clazz;
                sourceVisitor.binding = frag.resolveBinding().getDeclaringClass();
                field.expression = (CExpression) sourceVisitor.visitExpr(frag.getInitializer(), null);
                sourceVisitor.clazz = null;
            }
        }
    }

    public CNode visit(MethodDeclaration node, CClass clazz) {
        CMethod method = PreVisitor.visitMethod(node.resolveBinding(), clazz);
        //replace real names
        for (int i = 0; i < node.parameters().size(); i++) {
            SingleVariableDeclaration param = (SingleVariableDeclaration) node.parameters().get(i);
            CParameter cp = method.params.get(i);
            //todo check name
            cp.setName(CName.from(param.getName().getIdentifier()));
        }
        if (visitSource) {
            sourceVisitor.clazz = clazz;
            sourceVisitor.method = method;
            sourceVisitor.binding = node.resolveBinding().getDeclaringClass();
            method.body = (CBlockStatement) sourceVisitor.visitExpr(node.getBody(), null);
        }
        return method;
    }

    public void visit(Initializer node, CClass cc) {
        CMethod method = new CMethod();
        cc.addMethod(method);
        method.name = CName.from(Config.static_init_name);
        //todo main entry should call this
        Logger.log("static init in " + cc.getType().basicForm());
        method.type = TypeHelper.getVoidType();
        method.setStatic(true);
        sourceVisitor.method = method;
        sourceVisitor.clazz = cc;
        method.body = (CBlockStatement) sourceVisitor.visitExpr(node.getBody(), null);
    }
}
