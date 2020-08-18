package com.mesut.j2cpp.visitor;

import com.mesut.j2cpp.Converter;
import com.mesut.j2cpp.ast.*;
import com.mesut.j2cpp.cppast.CExpression;
import com.mesut.j2cpp.cppast.CFieldDef;
import com.mesut.j2cpp.cppast.CNode;
import com.mesut.j2cpp.cppast.CStatement;
import com.mesut.j2cpp.cppast.expr.CMethodInvocation;
import com.mesut.j2cpp.cppast.stmt.CBlockStatement;
import org.eclipse.jdt.core.dom.*;

import java.util.List;

public class SourceVisitor extends GenericVisitor<CNode, CNode> {

    Converter converter;
    CSource source;
    TypeVisitor typeVisitor;
    CClass clazz;

    public SourceVisitor(Converter converter, CSource source) {
        this.converter = converter;
        this.source = source;
        typeVisitor = new TypeVisitor(converter, source.header);
    }


    public void convert() {
        for (CClass clazz : source.header.classes) {
            this.clazz = clazz;
            for (CField field : clazz.fields) {
                Expression expression = field.node.getInitializer();
                if (expression != null) {
                    CFieldDef fieldDef = new CFieldDef(field);
                    fieldDef.setExpression((CExpression) visit(expression, null));
                    source.fieldDefs.add(fieldDef);
                }
            }
            for (CMethodDecl methodDecl : clazz.methods) {
                if (methodDecl.node.getBody() != null) {
                    CMethod methodDef = new CMethod();
                    methodDef.body = (CBlockStatement) visit(methodDecl.node.getBody(), null);
                    source.methods.add(methodDef);
                }
            }
        }
    }

    @Override
    public CNode visit(Block n, CNode arg) {
        CBlockStatement res = new CBlockStatement();
        for (Statement statement : (List<Statement>) n.statements()) {
            res.statements.add((CStatement) visit(statement, arg));
        }
        return res;
    }

    @Override
    public CNode visit(CompilationUnit node, CNode arg) {
        return null;
    }

    @Override
    public CNode visit(PackageDeclaration node, CNode arg) {
        return null;
    }

    @Override
    public CNode visit(ImportDeclaration node, CNode arg) {
        return null;
    }

    @Override
    public CNode visit(TypeDeclaration node, CNode arg) {
        return null;
    }

    @Override
    public CNode visit(EnumDeclaration node, CNode arg) {
        return null;
    }

    @Override
    public CNode visit(FieldDeclaration node, CNode arg) {
        return null;
    }

    @Override
    public CNode visit(MethodDeclaration node, CNode arg) {
        return null;
    }

    @Override
    public CNode visit(StringLiteral node, CNode arg) {
        return null;
    }

    @Override
    public CNode visit(BooleanLiteral node, CNode arg) {
        return null;
    }

    @Override
    public CNode visit(CharacterLiteral node, CNode arg) {
        return null;
    }

    @Override
    public CNode visit(NullLiteral node, CNode arg) {
        return null;
    }

    @Override
    public CNode visit(TypeLiteral node, CNode arg) {
        return null;
    }

    @Override
    public CNode visit(Assignment node, CNode arg) {
        return null;
    }

    @Override
    public CNode visit(BreakStatement node, CNode arg) {
        return null;
    }

    @Override
    public CNode visit(ContinueStatement node, CNode arg) {
        return null;
    }

    @Override
    public CNode visit(ReturnStatement node, CNode arg) {
        return null;
    }

    @Override
    public CNode visit(IfStatement node, CNode arg) {
        return null;
    }

    @Override
    public CNode visit(ForStatement node, CNode arg) {
        return null;
    }

    @Override
    public CNode visit(EnhancedForStatement node, CNode arg) {
        return null;
    }

    @Override
    public CNode visit(WhileStatement node, CNode arg) {
        return null;
    }

    @Override
    public CNode visit(DoStatement node, CNode arg) {
        return null;
    }

    @Override
    public CNode visit(YieldStatement node, CNode arg) {
        return null;
    }

    @Override
    public CNode visit(SwitchStatement node, CNode arg) {
        return null;
    }

    @Override
    public CNode visit(LabeledStatement node, CNode arg) {
        return null;
    }

    @Override
    public CNode visit(ConstructorInvocation node, CNode arg) {
        return null;
    }

    @Override
    public CNode visit(ExpressionStatement node, CNode arg) {
        return null;
    }

    @Override
    public CNode visit(SuperConstructorInvocation node, CNode arg) {
        return null;
    }

    @Override
    public CNode visit(SwitchCase node, CNode arg) {
        return null;
    }

    @Override
    public CNode visit(SynchronizedStatement node, CNode arg) {
        return null;
    }

    @Override
    public CNode visit(ThrowStatement node, CNode arg) {
        return null;
    }

    @Override
    public CNode visit(TryStatement node, CNode arg) {
        return null;
    }

    @Override
    public CNode visit(TypeDeclarationStatement node, CNode arg) {
        return null;
    }

    @Override
    public CNode visit(VariableDeclarationStatement node, CNode arg) {
        return null;
    }

    @Override
    public CNode visit(VariableDeclaration node, CNode arg) {
        return null;
    }

    @Override
    public CNode visit(FieldAccess node, CNode arg) {
        return null;
    }

    @Override
    public CNode visit(ClassInstanceCreation node, CNode arg) {
        return null;
    }

    @Override
    public CNode visit(ConditionalExpression node, CNode arg) {
        return null;
    }

    @Override
    public CNode visit(Name node, CNode arg) {
        return null;
    }

    @Override
    public CNode visit(SuperMethodInvocation node, CNode arg) {
        return null;
    }

    @Override
    public CNode visit(SwitchExpression node, CNode arg) {
        return null;
    }

    @Override
    public CNode visit(ThisExpression node, CNode arg) {
        return null;
    }

    @Override
    public CNode visit(VariableDeclarationExpression node, CNode arg) {
        return null;
    }

    @Override
    public CNode visit(NumberLiteral node, CNode arg) {
        return null;
    }

    @Override
    public CNode visit(SuperFieldAccess node, CNode arg) {
        return null;
    }

    @Override
    public CNode visit(InfixExpression node, CNode arg) {
        return null;
    }

    @Override
    public CNode visit(PostfixExpression node, CNode arg) {
        return null;
    }

    @Override
    public CNode visit(PrefixExpression node, CNode arg) {
        return null;
    }

    @Override
    public CNode visit(InstanceofExpression node, CNode arg) {
        source.hasRuntime = true;
        CMethodInvocation invocation = new CMethodInvocation();
        invocation.arguments.add((CExpression) visit(node.getLeftOperand(), arg));
        invocation.name = new CName("instance_of");
        CType type = typeVisitor.visitType(node.getRightOperand(), clazz);
        invocation.name.typeArgs.add(type);
        return invocation;
    }

    @Override
    public CNode visit(ArrayAccess node, CNode arg) {
        return null;
    }

    @Override
    public CNode visit(ArrayCreation node, CNode arg) {
        return null;
    }

    @Override
    public CNode visit(CastExpression node, CNode arg) {
        return null;
    }

    @Override
    public CNode visit(ParenthesizedExpression node, CNode arg) {
        return null;
    }

    @Override
    public CNode visit(ArrayInitializer node, CNode arg) {
        return null;
    }

    @Override
    public CNode visit(ArrayType node, CNode arg) {
        return null;
    }

    @Override
    public CNode visit(PrimitiveType node, CNode arg) {
        return null;
    }

    @Override
    public CNode visit(QualifiedType node, CNode arg) {
        return null;
    }

    @Override
    public CNode visit(SimpleType node, CNode arg) {
        return null;
    }

    @Override
    public CNode visit(SimpleName node, CNode arg) {
        return new CName(node.getIdentifier());
    }

    @Override
    public CNode visit(QualifiedName node, CNode arg) {
        return new CName(node.getFullyQualifiedName());
    }
}
