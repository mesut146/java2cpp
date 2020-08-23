package com.mesut.j2cpp.visitor;

import com.mesut.j2cpp.ast.*;
import com.mesut.j2cpp.cppast.CNode;
import com.mesut.j2cpp.cppast.stmt.CBlockStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;

import java.util.List;

public class DeclarationVisitor extends DefaultVisitor<CNode, CNode> {

    SourceVisitor sourceVisitor;
    TypeVisitor typeVisitor;

    public DeclarationVisitor(SourceVisitor sourceVisitor, TypeVisitor typeVisitor) {
        this.sourceVisitor = sourceVisitor;
        this.typeVisitor = typeVisitor;
    }

    @Override
    public CNode visit(MethodDeclaration node, CNode arg) {
        CClass clazz = (CClass) arg;
        CMethodDecl methodDecl = new CMethodDecl();
        CMethod method = new CMethod();
        method.decl = methodDecl;

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

        methodDecl.node = node;
        method.body = (CBlockStatement) sourceVisitor.visit(methodDecl.node.getBody(), null);
        return method;
    }
}
