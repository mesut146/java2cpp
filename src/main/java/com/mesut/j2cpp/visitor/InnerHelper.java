package com.mesut.j2cpp.visitor;

import com.mesut.j2cpp.Config;
import com.mesut.j2cpp.ast.*;
import com.mesut.j2cpp.cppast.expr.CAssignment;
import com.mesut.j2cpp.cppast.expr.CFieldAccess;
import com.mesut.j2cpp.cppast.expr.CThisExpression;
import com.mesut.j2cpp.cppast.stmt.CBlockStatement;
import com.mesut.j2cpp.cppast.stmt.CExpressionStatement;
import com.mesut.j2cpp.cppast.stmt.CReturnStatement;
import com.mesut.j2cpp.util.TypeHelper;

public class InnerHelper {
    //create reference of outer instance
    public static void handleRef(CClass cc, CClass outer) {
        CName refName = CName.from(Config.parentName);
        //make field for parent reference
        cc.addField(new CField(outer.getType(), refName));

        //assign expr
        CFieldAccess access = new CFieldAccess();
        access.isArrow = true;
        access.scope = new CThisExpression();
        access.name = refName;
        CExpressionStatement assign = new CExpressionStatement(new CAssignment(access, refName, "="));

        //make ref param
        CParameter parameter = new CParameter(outer.getType(), refName);

        if (Config.outer_ref_cons_arg) {
            //for every constructor add ref as parameter
            boolean hasDefCons = false;
            for (CMethod method : cc.methods) {
                if (method.isCons) {
                    if (method.params.isEmpty()) {
                        hasDefCons = true;
                    }

                    method.addParam(parameter);
                    method.body.addStatement(assign);
                }
            }
            //make def constructor if not present
            if (!hasDefCons) {
                CMethod cons = new CMethod();
                cons.isCons = true;
                cons.name = CName.from(cc.name);
                cons.body = new CBlockStatement();
                cc.addMethod(cons);
            }
        }
        else {
            CMethod method = new CMethod();
            method.setType(cc.getType());
            method.name = new CName(Config.refSetterName);
            method.setPublic(true);
            method.addParam(parameter);
            method.body = new CBlockStatement();
            method.body.addStatement(assign);
            method.body.addStatement(new CReturnStatement(new CThisExpression()));
            cc.addMethod(method);
        }
    }
}
