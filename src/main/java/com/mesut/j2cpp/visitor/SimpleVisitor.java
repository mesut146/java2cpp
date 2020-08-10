package com.mesut.j2cpp.visitor;

import com.mesut.j2cpp.cppast.CNode;
import org.eclipse.jdt.core.dom.*;

import java.util.List;

public class SimpleVisitor implements Visitor<CNode, Object> {

    <T> void visitNodes(List list) {

    }

    @Override
    public CNode visit(CompilationUnit node, Object arg) {
        visit(node.getPackage(), arg);
        for (ImportDeclaration imp : (List<ImportDeclaration>) node.imports()) {
            visit(imp, arg);
        }
        for (Object type : node.types()) {
            if (type instanceof TypeDeclaration) {
                visit((TypeDeclaration) type, arg);
            }
            else if (type instanceof EnumDeclaration) {
                visit((EnumDeclaration) type, arg);
            }
            else {
                throw new RuntimeException("incalid type decl = " + type);
            }
        }
        return null;
    }

    @Override
    public CNode visit(PackageDeclaration node, Object arg) {
        return null;
    }

    @Override
    public CNode visit(ImportDeclaration node, Object arg) {
        return null;
    }

    @Override
    public CNode visit(TypeDeclaration node, Object arg) {
        return null;
    }

    @Override
    public CNode visit(EnumDeclaration node, Object arg) {
        return null;
    }

    @Override
    public CNode visit(FieldDeclaration node, Object arg) {
        return null;
    }

    @Override
    public CNode visit(MethodDeclaration node, Object arg) {
        return null;
    }

    @Override
    public CNode visit(StringLiteral node, Object arg) {
        return null;
    }

    @Override
    public CNode visit(BooleanLiteral node, Object arg) {
        return null;
    }

    @Override
    public CNode visit(CharacterLiteral node, Object arg) {
        return null;
    }

    @Override
    public CNode visit(NullLiteral node, Object arg) {
        return null;
    }

    @Override
    public CNode visit(TypeLiteral node, Object arg) {
        return null;
    }

    @Override
    public CNode visit(Assignment node, Object arg) {
        return null;
    }

    @Override
    public CNode visit(BreakStatement node, Object arg) {
        return null;
    }

    @Override
    public CNode visit(ContinueStatement node, Object arg) {
        return null;
    }

    @Override
    public CNode visit(ReturnStatement node, Object arg) {
        return null;
    }

    @Override
    public CNode visit(IfStatement node, Object arg) {
        return null;
    }

    @Override
    public CNode visit(ForStatement node, Object arg) {
        return null;
    }

    @Override
    public CNode visit(EnhancedForStatement node, Object arg) {
        return null;
    }

    @Override
    public CNode visit(WhileStatement node, Object arg) {
        return null;
    }

    @Override
    public CNode visit(DoStatement node, Object arg) {
        return null;
    }

    @Override
    public CNode visit(YieldStatement node, Object arg) {
        return null;
    }

    @Override
    public CNode visit(SwitchStatement node, Object arg) {
        return null;
    }

    @Override
    public CNode visit(LabeledStatement node, Object arg) {
        return null;
    }

    @Override
    public CNode visit(VariableDeclaration node, Object arg) {
        return null;
    }

    @Override
    public CNode visit(FieldAccess node, Object arg) {
        return null;
    }

    @Override
    public CNode visit(ClassInstanceCreation node, Object arg) {
        return null;
    }

    @Override
    public CNode visit(InfixExpression node, Object arg) {
        return null;
    }

    @Override
    public CNode visit(PostfixExpression node, Object arg) {
        return null;
    }

    @Override
    public CNode visit(PrefixExpression node, Object arg) {
        return null;
    }

    @Override
    public CNode visit(InstanceofExpression node, Object arg) {
        return null;
    }

    @Override
    public CNode visit(ArrayAccess node, Object arg) {
        return null;
    }

    @Override
    public CNode visit(ArrayCreation node, Object arg) {
        return null;
    }

    @Override
    public CNode visit(CastExpression node, Object arg) {
        return null;
    }

    @Override
    public CNode visit(ParenthesizedExpression node, Object arg) {
        return null;
    }

    @Override
    public CNode visit(ArrayInitializer node, Object arg) {
        return null;
    }

    @Override
    public CNode visit(ArrayType node, Object arg) {
        return null;
    }

    @Override
    public CNode visit(PrimitiveType node, Object arg) {
        return null;
    }

    @Override
    public CNode visit(QualifiedType node, Object arg) {
        return null;
    }

    @Override
    public CNode visit(SimpleType node, Object arg) {
        return null;
    }
}
