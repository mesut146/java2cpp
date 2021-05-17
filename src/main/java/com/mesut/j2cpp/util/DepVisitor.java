package com.mesut.j2cpp.util;

import com.mesut.j2cpp.Logger;
import com.mesut.j2cpp.ast.CClass;
import com.mesut.j2cpp.map.ClassMap;
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
        declaration.accept(this);
    }

    void add(ITypeBinding binding, String expression) {
        if (binding == null) {
            Logger.logBinding(cc, expression);
            return;
        }
        //todo prevent header types being included again
        cc.addType(TypeVisitor.fromBinding(binding));
        ClassMap.sourceMap.init(binding);
    }

    @Override
    public boolean visit(ClassInstanceCreation node) {
        add(node.getType().resolveBinding(), node.toString());
        return super.visit(node);
    }

    @Override
    public boolean visit(MethodInvocation node) {
        if (node.getExpression() != null) {
            Expression scope = node.getExpression();
            if (scope.toString().equals("System.out") || scope.toString().equals("java.lang.System.out") || scope.toString().equals("System.err") || scope.toString().equals("java.lang.System.err")) {
                return super.visit(node);
            }
            add(scope.resolveTypeBinding(), node.toString());
        }
        return super.visit(node);
    }

    @Override
    public boolean visit(FieldAccess node) {
        add(node.resolveTypeBinding(), node.toString());
        return super.visit(node);
    }

    @Override
    public boolean visit(QualifiedName node) {
        //may be field access
        IBinding binding = node.resolveBinding();
        if (binding != null) {
            if (binding instanceof IVariableBinding) {
                add(node.getQualifier().resolveTypeBinding(), node.toString());
            }
        }
        return false;
    }

    @Override
    public boolean visit(CatchClause node) {
        add(node.getException().getType().resolveBinding(), node.getException().toString());
        return true;
    }

    @Override
    public boolean visit(CastExpression node) {
        add(node.getType().resolveBinding(), node.toString());
        return super.visit(node);
    }

    @Override
    public boolean visit(InstanceofExpression node) {
        add(node.getRightOperand().resolveBinding(), node.toString());
        return super.visit(node);
    }
}
