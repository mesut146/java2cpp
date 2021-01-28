package com.mesut.j2cpp.cppast;

import com.mesut.j2cpp.ast.CClass;
import com.mesut.j2cpp.ast.CField;
import com.mesut.j2cpp.ast.CMethod;
import com.mesut.j2cpp.ast.CType;
import com.mesut.j2cpp.util.PrintHelper;

import java.util.List;
import java.util.stream.Collectors;

//anonymous class definition in .cpp file
public class CClassImpl extends CExpression {
    public CClass clazz;

    public CClassImpl(CClass clazz) {
        this.clazz = clazz;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ");
        sb. append(clazz.name);

        if (!clazz.base.isEmpty()) {
            sb.append(" : public ");
            getScope(clazz.base);
            sb.append(clazz.base.stream().map(CType::toString).collect(Collectors.joining(", ")));
        }
        sb.append("{\n");
        up();

        printFields(sb);
        printMethods(sb);

        sb.append("};");
        sb.append("//").append(clazz.name).append("\n");
        return sb.toString();
    }

    private void printMethods(StringBuilder sb) {
        if (!clazz.methods.isEmpty()) {
            sb.append(getIndent()).append("//methods\n");
            List<CMethod> public_methods = clazz.methods.stream().filter(CMethod::isPublic).collect(Collectors.toList());
            List<CMethod> priv_methods = clazz.methods.stream().filter(CMethod::isPrivate).collect(Collectors.toList());
            printMethods(public_methods, "public:", sb);
            printMethods(priv_methods, "private:", sb);
            sb.append("\n");
        }
    }

    private void printMethods(List<CMethod> list, String modifier, StringBuilder sb) {
        if (!list.isEmpty()) {
            getScope(list);
            sb.append(modifier).append("\n");
            for (CMethod cm : list) {
                sb.append(PrintHelper.body(cm.toString(), getIndent()));
                sb.append("\n");
            }
        }
    }

    private void printFields(StringBuilder sb) {
        if (!clazz.methods.isEmpty()) {
            sb.append(getIndent()).append("//fields\n");
            List<CField> public_fields = clazz.fields.stream().filter(CField::isPublic).collect(Collectors.toList());
            List<CField> priv_fields = clazz.fields.stream().filter(CField::isPrivate).collect(Collectors.toList());
            printFields(public_fields, "public:", sb);
            printFields(priv_fields, "private:", sb);
            sb.append("\n");
        }
    }

    private void printFields(List<CField> list, String modifier, StringBuilder sb) {
        if (list.size() > 0) {
            getScope(list);
            sb.append(modifier).append("\n");
            for (CField cf : list) {
                sb.append(PrintHelper.body(cf.toString(), getIndent()));
                sb.append("\n");
            }
        }
    }

}
