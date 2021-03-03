package com.mesut.j2cpp.util;

import com.mesut.j2cpp.Logger;
import com.mesut.j2cpp.ast.CClass;
import com.mesut.j2cpp.ast.CType;
import com.mesut.j2cpp.map.ClassMap;
import com.mesut.j2cpp.visitor.PreVisitor;
import com.mesut.j2cpp.visitor.TypeVisitor;
import org.eclipse.jdt.core.dom.*;

//visit complete types and include them,others already forward declared
public class DepVisitor extends ASTVisitor {
    CClass cc;
    AbstractTypeDeclaration declaration;

    public DepVisitor(CClass cc, AbstractTypeDeclaration decl) {
        this.cc = cc;
        this.declaration = decl;
    }

    public void handle() {
        cc.addType(cc.superClass);
        for (CType type : cc.ifaces) {
            cc.addType(type);
        }
        declaration.accept(this);
    }

    void add(ITypeBinding binding, Expression expression) {
        if (binding == null) {
            Logger.logBinding(cc, expression.toString());
            return;
        }
        cc.addType(TypeVisitor.fromBinding(binding));
        ClassMap.sourceMap.init(binding);
    }

    @Override
    public boolean visit(ClassInstanceCreation node) {
        add(node.getType().resolveBinding(), node);
        return super.visit(node);
    }

    @Override
    public boolean visit(MethodInvocation node) {
        if (node.getExpression() != null) {
            Expression scope = node.getExpression();
            add(scope.resolveTypeBinding(), node);
        }
        return super.visit(node);
    }

    @Override
    public boolean visit(FieldAccess node) {
        add(node.resolveTypeBinding(), node);
        return super.visit(node);
    }
}
