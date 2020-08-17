package com.mesut.j2cpp.visitor;

import com.mesut.j2cpp.ast.Node;
import com.mesut.j2cpp.cppast.CNode;
import org.eclipse.jdt.core.dom.*;

import java.util.List;

public class GenericVisitor<R, A> {

    private void visitNodes(List nodes, A arg) {
        for (Object node : nodes) {
            visit((ASTNode) node, arg);
        }
    }

    //statements
    public R visit(CompilationUnit node, A arg) {
        R res = visit(node.getPackage(), arg);
        visitNodes(node.imports(), arg);
        visitNodes(node.types(), arg);
        return res;
    }

    private R visit(ASTNode node, A arg) {
        if (node instanceof CompilationUnit) {
            return visit((CompilationUnit) node, arg);
        }

        return null;
    }

    public R visit(PackageDeclaration node, A arg) {
        return visit(node.getName(), arg);
    }

    public R visit(TypeDeclaration node, A arg) {
        return null;
    }

    public R visit(MethodDeclaration n, CNode arg) {
        return null;
    }

    public R visit(FieldDeclaration n, CNode arg) {
        return null;
    }

    public R visit(Name name, A arg) {
        return null;
    }

    /*public R visit(Statement n, A arg) {
        return null;
    }*/

    public R visit(ExpressionStatement n, A arg) {
        return visit(n.getExpression(), arg);
    }

    public R visit(Block n, A arg) {
        for (Statement statement : (List<Statement>) n.statements()) {
            //visit(statement,arg);
        }
        return null;
    }

    public R visit(ForStatement n, A arg) {
        //n.initializers().forEach(init -> visit(init, arg));
        visit(n.getExpression(), arg);
        //visit(n.getBody(), arg);
        return null;
    }

    public R visit(EnhancedForStatement n, A arg) {
        //n.initializers().forEach(init -> visit(init, arg));
        visit(n.getExpression(), arg);
        //visit(n.getBody(), arg);
        return null;
    }

    public R visit(WhileStatement n, A arg) {
        visit(n.getExpression(), arg);
        return null;
    }

    public R visit(IfStatement n, A arg) {
        return null;
    }

    public R visit(ReturnStatement n, A arg) {
        return null;
    }

    public R visit(TryStatement n, A arg) {
        return null;
    }

    public R visit(ThrowStatement n, A arg) {
        return null;
    }

    //expressions

    public R visit(Expression n, A arg) {
        return null;
    }

    public R visit(MethodInvocation n, A arg) {
        return null;
    }

    public R visit(FieldAccess n, A arg) {
        return null;
    }

}
