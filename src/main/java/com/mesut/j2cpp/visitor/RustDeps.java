package com.mesut.j2cpp.visitor;

import com.mesut.j2cpp.map.Mapper;
import org.eclipse.jdt.core.dom.*;

import java.util.HashSet;
import java.util.Set;

public class RustDeps extends ASTVisitor {
    Set<ITypeBinding> set = new HashSet<>();
    Code code;
    boolean writedIncludes = false;

    public RustDeps(Code code) {
        this.code = code;
    }

    public void handle(AbstractTypeDeclaration declaration) {
        declaration.accept(this);
    }

    void add(ITypeBinding binding, String expression) {
        if (binding == null || set.contains(binding)) return;
        var mapped = Mapper.instance.findClass(binding);
        if (mapped != null && !mapped.includes.isEmpty() && !writedIncludes) {
            for (String inc : mapped.includes) {
                code.line("%s\n", inc);
                writedIncludes = true;

            }
        }
        if (!binding.isFromSource()) return;
        set.add(binding);
        code.line("use crate::%s;\n", binding.getQualifiedName().replace(".", "::"));
    }

    @Override
    public boolean visit(ClassInstanceCreation node) {
        add(node.getType().resolveBinding(), node.toString());
        var info = Mapper.instance.findInfo(node.resolveConstructorBinding());
        if (info != null) {
            for (var inc : info.includes) {
                code.line("%s\n", inc);
            }
        }
        return super.visit(node);
    }

    @Override
    public boolean visit(MethodDeclaration node) {
        if (Rust.isMain(node.resolveBinding())) {
            code.line("use std::env;\n");
        }
        return super.visit(node);
    }

    @Override
    public boolean visit(MethodInvocation node) {
        if (node.getExpression() != null) {
            Expression scope = node.getExpression();
            var bind = node.resolveMethodBinding();
            var info = Mapper.instance.findInfo(bind);
            if (info != null) {
                for (var inc : info.includes) {
                    code.line("%s\n", inc);
                }
            }
            String str = scope.toString();
            if (str.equals("System.out") || str.equals("java.lang.System.out") || str.equals("System.err") || str.equals("java.lang.System.err")) {
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
