package com.mesut.j2cpp.visitor;

import org.eclipse.jdt.core.dom.*;

import java.util.HashSet;
import java.util.Set;

//visit complete types and include them,others already forward declared
public class RustDeps extends ASTVisitor {
    AbstractTypeDeclaration declaration;
    Set<ITypeBinding> set = new HashSet();

    public RustDeps(AbstractTypeDeclaration decl) {
        this.declaration = decl;
    }

    public void handle() {
        declaration.accept(this);
    }

    void add(ITypeBinding binding, String expression) {
        if (binding == null) return;
        //todo prevent header types being included again
        set.add(binding);
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
