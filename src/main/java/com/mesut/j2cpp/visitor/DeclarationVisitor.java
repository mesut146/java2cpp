package com.mesut.j2cpp.visitor;

import com.mesut.j2cpp.ast.CMethodDecl;
import com.mesut.j2cpp.ast.CName;
import com.mesut.j2cpp.ast.CParameter;
import com.mesut.j2cpp.ast.CType;
import com.mesut.j2cpp.cppast.CNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;

import java.util.List;

public class DeclarationVisitor extends DefaultVisitor<CNode, CNode> {

    TypeVisitor typeVisitor;

    public DeclarationVisitor(TypeVisitor typeVisitor) {
        this.typeVisitor = typeVisitor;
    }

    @Override
    public CNode visit(MethodDeclaration node, CNode arg) {
        CMethodDecl method = new CMethodDecl();

        node.typeParameters().forEach(temp -> method.template.add(new CType(temp.toString())));

        if (node.isConstructor()) {
            method.isCons = true;
        }
        else {
            Type type = node.getReturnType2();
            if (type == null) {
                method.isCons = true;
            }
            else {
                method.type = typeVisitor.visit(node.getReturnType2());
            }
        }

        //type could be template
        method.name = new CName(node.getName().getIdentifier());

        method.setStatic(Modifier.isStatic(node.getModifiers()));
        method.setPublic(Modifier.isPublic(node.getModifiers()));
        method.setNative(Modifier.isNative(node.getModifiers()));
        if (last().isInterface) {
            method.setPublic(true);
            method.isPureVirtual = true;
        }

        for (SingleVariableDeclaration param : (List<SingleVariableDeclaration>) node.parameters()) {
            CParameter cp = new CParameter();
            cp.type = typeVisitor.visitType(param.getType(), last());
            cp.type.isTemplate = false;
            cp.setName(param.getName().getIdentifier());
            method.params.add(cp);
        }

        method.node = node;
        return method;
    }
}
