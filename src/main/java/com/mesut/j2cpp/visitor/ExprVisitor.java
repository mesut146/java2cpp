package com.mesut.j2cpp.visitor;

import com.mesut.j2cpp.Converter;
import com.mesut.j2cpp.Writer;
import com.mesut.j2cpp.ast.*;
import org.eclipse.jdt.core.dom.*;

import java.util.Iterator;
import java.util.List;

public class ExprVisitor extends ASTVisitor {

    public CMethod method;
    public CClass clazz;
    public CHeader header;
    public Converter converter;
    TypeVisitor typeVisitor;
    Writer w;

    public ExprVisitor(Converter converter, CHeader header, TypeVisitor typeVisitor) {
        this.converter = converter;
        this.header = header;
        this.typeVisitor = typeVisitor;
    }

    public void setMethod(CMethod method) {
        this.method = method;
        //this.w = method.bodyWriter;
    }

    public void args(List<Expression> expressions, Writer w) {
        w.append("(");
        for (int i = 0; i < expressions.size(); i++) {
            expressions.get(i).accept(this);
            if (i < expressions.size() - 1) {
                w.append(",");
            }
        }
        w.append(")");
    }

    @Override
    public boolean visit(SimpleName n) {
        //w.append("#simple.name " + n.isDeclaration() + "," + n.isVar() + " ");
        w.append(n.getIdentifier());
        return false;
    }

    public boolean visit(QualifiedName name) {
        //todo array.length
        w.append(name.getFullyQualifiedName());
        return false;
    }

    @Override
    public boolean visit(InstanceofExpression n) {
        header.addRuntime();
        w.append("instance_of<");
        CType type = typeVisitor.visitType(n.getRightOperand(), method);
        w.append(type);
        w.append(">(");
        n.getLeftOperand().accept(this);
        w.append(")");
        return false;
    }

    @Override
    public boolean visit(MethodInvocation n) {
        if (n.getExpression() != null) {//scope
            Expression scope = n.getExpression();
            scope.accept(this);

            if (scope instanceof Name) {//field or class or variable
                String scopeName = ((Name) scope).getFullyQualifiedName();
                if (scopeName.equals("this")) {
                    w.append("->");
                    w.append(n.getName().getFullyQualifiedName());
                    args((List<Expression>) n.arguments(), w);
                    return false;
                }

                IMethodBinding binding = n.resolveMethodBinding();
                //System.out.println("call " + scopeName + " " + n.getName() + " " + scope.resolveTypeBinding());
                if (binding != null && Modifier.isStatic(binding.getModifiers())) {
                    w.append("::");
                }
                else {
                    w.append("->");
                }
            }
            else {
                //another method call or field access
                w.append("->");
            }
        }
        w.append(n.getName().getIdentifier());
        args(n.arguments(), w);
        return false;
    }

    public boolean visit(FieldAccess n) {
        Expression scope = n.getExpression();
        scope.accept(this);

        if (scope instanceof Name) {//field/var or class
            String scopeName = ((Name) scope).getFullyQualifiedName();
            if (scopeName.equals("this")) {
                w.append("->");
                w.append(n.getName().getIdentifier());
                return false;
            }
            IVariableBinding binding = n.resolveFieldBinding();

            if (binding.isEnumConstant()) {
                w.append("::");
            }
            else {
                if (Modifier.isStatic(binding.getModifiers())) {
                    w.append("::");
                }
                else {
                    w.append("->");
                }
            }

        }
        else {//another expr
            w.append("->");
        }
        w.append(n.getName().getIdentifier());
        return false;
    }

    //array[index] -> (*array)[index] (old)
    //array[index] -> array.get(index) (new)
    public boolean visit(ArrayAccess n) {
        /*w.append("(*");
        n.getArray().accept(this);
        w.append(")[");
        n.getIndex().accept(this);
        w.append("]");*/
        n.getArray().accept(this);
        w.append("->get(");
        n.getIndex().accept(this);
        w.append(")");
        return false;
    }

    @Override
    public boolean visit(Assignment n) {
        n.getLeftHandSide().accept(this);
        w.append(" ");
        w.append(n.getOperator().toString());
        w.append(" ");
        n.getRightHandSide().accept(this);
        return false;
    }

    @Override
    public boolean visit(ThisExpression n) {
        w.append("this");
        return false;
    }


    //new Base.Inner(args...){body}
    /*public Object visit(ObjectCreationExpr n, Writer w) {
        if (n.getScope().isPresent()) {
            n.getScope().get().accept(this, w);
            w.append("->");
        }
        w.append("new ");
        //typearg
        CType type;
        if (method == null) {
            type = typeVisitor.visitType(n.getType(), clazz);
        }
        else {
            type = typeVisitor.visitType(n.getType(), method);
        }

        type.isPointer = false;//new keyword already makes it pointer
        w.append(type.toString());
        args(n.getArguments(), w);
        if (n.getAnonymousClassBody().isPresent()) {
            //TODO
        }
        return null;
    }*/

    //expr.new type(args)
    @Override
    public boolean visit(ClassInstanceCreation node) {
        if (node.getExpression() != null) {
            w.append(node.getExpression().toString());
        }
        w.append("new ");
        CType type = typeVisitor.visitType(node.getType(), method);
        w.append(type);
        args(node.arguments(), w);
        return false;
    }

    //Type name=value
    @Override
    public boolean visit(VariableDeclarationExpression node) {
        //mostly in for
        //w.append("var.decl.expr ");
        boolean first = true;
        CType type = typeVisitor.visitType(node.getType(), method);
        type.isTemplate = false;
        for (VariableDeclarationFragment frag : (List<VariableDeclarationFragment>) node.fragments()) {
            if (first) {
                first = false;
                w.append(type);
                w.append(" ");
            }
            else {
                w.append(",");
            }
            w.append(frag.getName().getIdentifier());
            if (frag.getInitializer() != null) {
                w.append(" = ");
                frag.getInitializer().accept(this);
            }
        }
        return false;
    }

    public boolean visit(ConditionalExpression n) {
        n.getExpression().accept(this);
        w.append("?");
        n.getThenExpression().accept(this);
        w.append(":");
        n.getElseExpression().accept(this);
        return false;
    }

    @Override
    public boolean visit(InfixExpression node) {
        node.getLeftOperand().accept(this);
        w.append(" ");
        w.append(node.getOperator().toString());
        w.append(" ");
        node.getRightOperand().accept(this);
        return false;
    }

    @Override
    public boolean visit(PostfixExpression node) {
        node.getOperand().accept(this);
        w.append(node.getOperator().toString());
        return false;
    }

    @Override
    public boolean visit(PrefixExpression node) {
        w.append(node.getOperator().toString());
        node.getOperand().accept(this);
        return false;
    }

    /*@Override
    public boolean visit(SwitchExpression node) {
        w.append("switch(");
        node.getExpression().accept(this);
        w.append(")");
        for(Statement statement:(List<Statement>)node.statements()){
            //statement.accept();
        }
        return false;
    }*/


    @Override
    public boolean visit(NullLiteral n) {
        w.append("nullptr");
        return false;
    }

    @Override
    public boolean visit(NumberLiteral n) {
        w.append(n.getToken());
        return false;
    }

    @Override
    public boolean visit(CharacterLiteral n) {
        String str = n.getEscapedValue();
        /*if (str.startsWith("\\u")) {
            w.append("u");
        }*/
        //w.append("'");//str already has quotes
        w.append(str);
        //w.append("'");
        return false;
    }

    @Override
    public boolean visit(CastExpression n) {
        w.append("(");
        CType type = typeVisitor.visitType(n.getType(), method);
        w.append(type);
        /*if (type.isPointer()) {
            w.append("*");
        }*/
        w.append(")");
        n.getExpression().accept(this);
        return false;
    }

    @Override
    public boolean visit(ParenthesizedExpression n) {
        w.append("(");
        n.getExpression().accept(this);
        w.append(")");
        return false;
    }


    public boolean visit(StringLiteral n) {
        //string1(n);
        string2(n);
        return false;
    }

    private void string1(StringLiteral n) {
        ClassInstanceCreation creation = n.getAST().newClassInstanceCreation();
        StringLiteral literal = n.getAST().newStringLiteral();
        literal.setLiteralValue(n.getLiteralValue());
        creation.arguments().add(literal);
        creation.setType(n.getAST().newSimpleType(n.getAST().newName("java.lang.String")));
        visit(creation);//causes overflow
    }

    private void string2(StringLiteral n) {
        w.append("new java::lang::String(\"");
        w.append(n.getLiteralValue());
        w.append("\")");
    }

    @Override
    public boolean visit(BooleanLiteral n) {
        if (n.booleanValue()) {
            w.append("true");
        }
        else {
            w.append("false");
        }
        return false;
    }

    //new int[]{...} or new int[5], or {1,2,3}
    public boolean visit(ArrayCreation n) {
        if (n.getInitializer() != null) {
            //just print values,ignore `new type[]`... because c++ allows it
            n.getInitializer().accept(this);
        }
        else {
            //only dimensions
            w.append("new ");
            CType typeName = typeVisitor.visitType(n.getType(), method).copy();
            typeName.dimensions = n.dimensions().size();
            w.append(typeName.toString());
            w.append("(");
            w.append("new int[]{");

            for (Iterator<Expression> iterator = n.dimensions().iterator(); iterator.hasNext(); ) {
                Expression dimension = iterator.next();
                if (dimension != null) {
                    dimension.accept(this);
                }
                else {
                    w.append("0");//default array size
                }
                if (iterator.hasNext()) {
                    w.append(",");
                }
            }
            w.append("},");
            w.append(String.valueOf(typeName.dimensions));
            w.append(")");
        }
        return false;
    }

    //{{1,2,3},{4,5,6}}
    public boolean visit(ArrayInitializer n) {
        w.append("{");
        for (Iterator<Expression> iterator = n.expressions().iterator(); iterator.hasNext(); ) {
            Expression expr = iterator.next();
            expr.accept(this);
            if (iterator.hasNext()) {
                w.append(",");
            }
        }
        w.append("}");
        return false;
    }

    @Override
    public boolean visit(SuperConstructorInvocation node) {
        Writer writer = new Writer();
        Call call = new Call();
        for (Object o : node.arguments()) {

        }
        call.str = writer.toString();
        method.superCall = call;
        return false;
    }


}
