package com.mesut.j2cpp.cppast;

import com.mesut.j2cpp.ast.CClass;
import com.mesut.j2cpp.ast.CField;
import com.mesut.j2cpp.ast.CMethod;
import com.mesut.j2cpp.ast.CType;

import java.util.List;
import java.util.stream.Collectors;

//anonymous class definition in .cpp file
public class CClassImpl extends CExpression {
    public CClass clazz;

    public CClassImpl(CClass clazz) {
        this.clazz = clazz;
    }

    @Override
    public void print() {
        clear();
        append("class ");
        append(clazz.name);

        if (!clazz.base.isEmpty()) {
            append(" : public ");
            append(clazz.base.stream().map(CType::toString).collect(Collectors.joining(", ")));
        }
        appendln("{");
        up();

        printFields();
        printMethods();

        down();//public
        line("};");
        append("//" + clazz.name);
    }

    private void printMethods() {
        if (!clazz.methods.isEmpty()) {
            line("//methods");
        }
        List<CMethod> public_methods = clazz.methods.stream().filter(CMethod::isPublic).collect(Collectors.toList());
        List<CMethod> priv_methods = clazz.methods.stream().filter(CMethod::isPrivate).collect(Collectors.toList());
        printMethods(public_methods, "public:");
        printMethods(priv_methods, "private:");
        println();
    }

    private void printMethods(List<CMethod> list, String modifier) {
        if (list.size() > 0) {
            line(modifier);
            up();
            for (CMethod cm : list) {
                cm.printAll(true);
                appendIndent(cm);
                println();
            }
            down();
        }
    }

    private void printFields() {
        if (!clazz.methods.isEmpty()) {
            line("//fields");
        }
        List<CField> public_fields = clazz.fields.stream().filter(CField::isPublic).collect(Collectors.toList());
        List<CField> priv_fields = clazz.fields.stream().filter(CField::isPrivate).collect(Collectors.toList());
        printFields(public_fields, "public:");
        printFields(priv_fields, "private:");
        println();
    }

    private void printFields(List<CField> list, String modifier) {
        if (list.size() > 0) {
            line(modifier);
            up();
            for (CField cf : list) {
                appendIndent(cf);
            }
            down();
        }
    }

}
