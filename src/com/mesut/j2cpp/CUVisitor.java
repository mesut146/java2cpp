package com.mesut.j2cpp;

import com.mesut.j2cpp.parser.*;
import org.antlr.v4.runtime.tree.*;
import com.mesut.j2cpp.ast.*;
import java.util.*;
import com.mesut.j2cpp.parser.Java8Parser.*;
import com.mesut.j2cpp.visitor.*;

public class CUVisitor extends Java8ParserBaseVisitor
{

    CHeader h;
    Stack<CClass> stack=new Stack<>();
    Common common=new Common();

    @Override
    public Object visitPackageDeclaration(Java8Parser.PackageDeclarationContext ctx)
    {
        //System.out.println("pkg");
        //namespace
        Namespace ns=new Namespace();
        ns.pkg(ctx.packageName().getText());
        h.ns = ns;
        return ns;
    }

    CClass last()
    {
        return stack.peek();
    }

    @Override
    public Object visitNormalClassDeclaration(Java8Parser.NormalClassDeclarationContext ctx)
    {
        CClass cc=new CClass();
        cc.isInterface = false;
        //last=cc;
        if (stack.size() == 0)
        {
            stack.push(cc);
            h.addClass(cc);
        }
        else
        {
            last().addInner(cc);
            stack.push(cc);
        }

        cc.name = ctx.Identifier().getText();

        if (ctx.superclass() != null)
        {//extends
            cc.base.add(ctx.superclass().classType().getText());
        }

        if (ctx.superinterfaces() != null)
        {
            typeList(ctx.superinterfaces().interfaceTypeList(), cc);
        }
        for (ClassBodyDeclarationContext cbdc:ctx.classBody().classBodyDeclaration())
        {
            visitClassBodyDeclaration(cbdc);
        }
        stack.pop();
        return cc;
    }

    @Override
    public Object visitNormalInterfaceDeclaration(Java8Parser.NormalInterfaceDeclarationContext ctx)
    {
        //System.out.println("iface dec");
        CClass cc=new CClass();
        //last=cc;
        if (stack.size() == 0)
        {
            stack.push(cc);
            h.addClass(cc);
        }
        else
        {
            last().addInner(cc);
            stack.push(cc);
        }
        cc.name = ctx.Identifier().getText();
        cc.isInterface = true;
        if (ctx.extendsInterfaces() != null)
        {
            typeList(ctx.extendsInterfaces().interfaceTypeList(), cc);
        }
        visit(ctx.interfaceBody());
        stack.pop();
        return cc;
    }

    //iface field
    @Override
    public Object visitConstantDeclaration(Java8Parser.ConstantDeclarationContext ctx)
    {
        //System.out.println("const dec="+ctx.getText());
        for (VariableDeclaratorContext cdc:ctx.variableDeclaratorList().variableDeclarator())
        {
            CField cf=new CField();
            cf.type = ctx.unannType().getText();
            cf.name = cdc.variableDeclaratorId().getText();
            cf.right = cdc.variableInitializer().getText();
            last().addField(cf);

        }
        return null;
    }

    @Override
    public Object visitInterfaceMethodDeclaration(Java8Parser.InterfaceMethodDeclarationContext ctx)
    {
        //System.out.println("iface meth");
        CMethod cm=new CMethod();
        cm.empty = true;
        last().methods.add(cm);
        for (InterfaceMethodModifierContext immc:ctx.interfaceMethodModifier())
        {
            cm.modifiers.add(immc.getText());
        }
        cm.type = ctx.methodHeader().result().getText();
        cm.name = ctx.methodHeader().methodDeclarator().Identifier().getText();

        param(ctx.methodHeader().methodDeclarator().formalParameterList(), cm);

        //visit(ctx.methodBody());
        return cm;
    }



    void param(FormalParameterListContext fplc, CMethod cm)
    {
        if (fplc != null)
        {
            if (fplc.receiverParameter() != null)
            {
                cm.params.add((CParameter)common.visitReceiverParameter(fplc.receiverParameter()));
            }
            else if (fplc.formalParameters() != null)
            {
                for (FormalParameterContext fpc2:fplc.formalParameters().formalParameter())
                {     
                    cm.params.add((CParameter)common.visitFormalParameter(fpc2));
                }
            }

            LastFormalParameterContext lfpc=fplc.lastFormalParameter();
            if (lfpc != null)
            {
                if (lfpc.formalParameter() != null)
                {
                    cm.params.add((CParameter)common.visitFormalParameter(lfpc.formalParameter()));
                }
                else
                {
                    //TODO could be array
                    CParameter cp=new CParameter();
                    //System.out.println("lfpc=" + lfpc.unannType());
                    cp.type = lfpc.unannType().getText();
                    cp.name = lfpc.variableDeclaratorId().getText();
                    cm.params.add(cp);
                }
            }
        }
    }

    void typeList(InterfaceTypeListContext ctx, CClass cc)
    {
        for (InterfaceTypeContext itc:ctx.interfaceType())
        {
            cc.base.add(itc.getText());
        }
    }

    @Override
    public Object visitFieldDeclaration(Java8Parser.FieldDeclarationContext ctx)
    {
        //System.out.println("field");
        String jtype=ctx.unannType().getText();

        String ctype;
        if (Helper.is(jtype))
        {
            ctype = Helper.getType(jtype);

        }
        else
        {
            ctype = jtype;
        }
        for (VariableDeclaratorContext vdc:ctx.variableDeclaratorList().variableDeclarator())
        {
            CField cf=new CField();
            cf.name = vdc.variableDeclaratorId().getText();
            cf.type = ctype;
            cf.isPublic = true;
            last().addField(cf);
        }
        return null;
    }

    @Override
    public Object visitMethodDeclaration(Java8Parser.MethodDeclarationContext ctx)
    {
        CMethod cm=new CMethod();
        last().addMethod(cm);
        cm.type = ctx.methodHeader().result().getText();
        cm.name = ctx.methodHeader().methodDeclarator().Identifier().getText();
        param(ctx.methodHeader().methodDeclarator().formalParameterList(), cm);
        //visit(ctx.methodBody());
        MethodVisitor mv=new MethodVisitor();
        mv.cm = cm;
        mv.body=cm.body;
        //cm.visitor=mv;
        mv.visitMethodBody(ctx.methodBody());
        return cm;
    }

    @Override
    public Object visitConstructorDeclaration(Java8Parser.ConstructorDeclarationContext ctx)
    {
        CMethod cm=new CMethod();
        last().addMethod(cm);
        cm.name = ctx.constructorDeclarator().simpleTypeName().getText();
        cm.isCons = true;
        param(ctx.constructorDeclarator().formalParameterList(), cm);

        MethodVisitor mv=new MethodVisitor();
        mv.cm = cm;
        mv.body=cm.body;
        //cm.visitor=mv;
        //Helper.debug(ctx.constructorBody());
        mv.visit(ctx.constructorBody());
        return cm;
    }




}
