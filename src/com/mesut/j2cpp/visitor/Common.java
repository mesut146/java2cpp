package com.mesut.j2cpp.visitor;
import com.mesut.j2cpp.*;
import com.mesut.j2cpp.ast.*;
import com.mesut.j2cpp.parser.*;

public class Common extends Java8ParserBaseVisitor
{

    @Override
    public Object visitReceiverParameter(Java8Parser.ReceiverParameterContext ctx)
    {
        CParameter cp=new CParameter();
        cp.type=new TypeName(ctx.unannType().getText());
        if(ctx.Identifier()!=null){
            cp.name=ctx.Identifier().getText();
        }
        return cp;
    }

    @Override
    public Object visitFormalParameters(Java8Parser.FormalParametersContext ctx)
    {
        // TODO: Implement this method
        return super.visitFormalParameters(ctx);
    }
    
    

    @Override
    public Object visitFormalParameter(Java8Parser.FormalParameterContext ctx)
    {
        CParameter cp=new CParameter();
        cp.type=new TypeName(ctx.unannType().getText());
        if(Helper.is(cp.type.toString())){
            cp.isPointer=false;
        }
        cp.name=ctx.variableDeclaratorId().getText();
        return cp;
    }
    
    
    
}
