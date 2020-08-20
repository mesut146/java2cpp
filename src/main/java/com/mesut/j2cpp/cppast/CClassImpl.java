package com.mesut.j2cpp.cppast;

import com.mesut.j2cpp.ast.CClass;
import com.mesut.j2cpp.ast.CMethodDecl;

//anonymous class definition in .cpp file
public class CClassImpl extends CExpression {
    public CClass clazz;

    public CClassImpl(CClass clazz) {
        this.clazz = clazz;
    }


    @Override
    public void print() {
        append("class ");
        append(clazz.name);
        if (!clazz.base.isEmpty()) {
            append(" : public ");
            for (int i = 0; i < clazz.base.size(); i++) {
                append(clazz.base.get(i).normal());
                if (i < clazz.base.size() - 1) {
                    append(", ");
                }
            }
        }
        appendln("{");
        up();
        line("public:");
        for (CMethodDecl decl : clazz.methods) {
            //todo
        }
        down();
        line("};");
        append("//" + clazz.name);
    }
}
