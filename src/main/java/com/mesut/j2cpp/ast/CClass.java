package com.mesut.j2cpp.ast;

import com.mesut.j2cpp.Config;
import com.mesut.j2cpp.cppast.CStatement;
import com.mesut.j2cpp.util.PrintHelper;
import com.mesut.j2cpp.util.TypeHelper;

import java.util.*;
import java.util.stream.Collectors;

public class CClass extends CStatement {

    public String name;
    public boolean isInterface = false;
    public boolean isStatic = false;
    public boolean isAnonymous = false;
    public Namespace ns = null;
    public List<CType> base = new ArrayList<>();
    public Template template = new Template();
    public List<CField> fields = new ArrayList<>();
    public List<CMethod> methods = new ArrayList<>();
    public List<CClass> classes = new ArrayList<>();
    public CClass parent;//outer
    public CHeader header;
    public Node staticBlock = null;
    CType type;
    Set<CType> types = new HashSet<>();

    public CClass() {
        if (Config.baseClassObject) {
            base.add(TypeHelper.getObjectType());
        }
    }

    public void addBase(CType... type) {
        Collections.addAll(base, type);
    }

    public void addInner(CClass cc) {
        cc.parent = this;
        cc.header = header;
        cc.ns = ns;
        classes.add(cc);
    }

    public void addMethod(CMethod cm) {
        cm.parent = this;
        methods.add(cm);
    }

    public void addField(CField cf) {
        cf.parent = this;
        fields.add(cf);
    }

    //add reference type that this class use
    public void addType(CType type) {
        types.add(type);
    }

    public boolean hasType(CClass type) {
        return types.contains(type.getType());
    }

    public Template getTemplate() {
        return template;
    }

    public CType getSuper() {
        if (base.isEmpty()) {
            return null;
        }
        return base.get(0);
    }

    public CMethod getMethod(boolean cons, String name, CType... params) {
        for (CMethod method : methods) {
            if (method.isCons == cons && (cons || method.name.name.equals(name)) && method.params.size() == params.length) {
                boolean found = true;
                for (int i = 0; i < params.length; i++) {
                    if (!method.params.get(i).type.equals(params[i])) {
                        found = false;
                        break;
                    }
                }
                if (found) {
                    return method;
                }
            }
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        printDecl(sb);
        sb.append("{\n");
        up();
        //impl
        if (staticBlock != null) {
            sb.append("\n\n");
            sb.append(getIndent()).append(staticBlock);
            sb.append("\n");
        }
        printFields(sb);
        printMethods(sb);

        //inner classes
        for (CClass cc : classes) {
            sb.append(PrintHelper.body(cc.toString(), getIndent()));
        }
        sb.append("};//class ").append(name);
        return sb.toString();
    }

    private void printDecl(StringBuilder sb) {
        getScope(template);
        getScope(base);
        if (isInterface) {
            sb.append("/*interface*/\n");
        }
        if (!template.isEmpty()) {
            sb.append("\n").append(template);
        }
        //class decl
        sb.append("class ").append(name);
        if (base.size() > 0) {
            getScope(base);
            sb.append(": public ");
            sb.append(PrintHelper.joinStr(base, ", "));
        }
        else {
            sb.append("\n");
        }
    }

    private void printMethods(StringBuilder sb) {
        if (!methods.isEmpty()) {
            sb.append("//methods\n");
            if (Config.methods_public) {
                printMethods(methods, "public:", sb);
            }
            else {
                List<CMethod> public_methods = methods.stream().filter(CMethod::isPublic).collect(Collectors.toList());
                List<CMethod> priv_methods = methods.stream().filter(CMethod::isPrivate).collect(Collectors.toList());
                printMethods(public_methods, "public:", sb);
                printMethods(priv_methods, "private:", sb);
            }
            sb.append("\n");
        }
    }

    private void printMethods(List<CMethod> list, String modifier, StringBuilder sb) {
        if (!list.isEmpty()) {
            getScope(list);
            sb.append(modifier).append("\n");
            for (CMethod cm : list) {
                sb.append(PrintHelper.body(cm.toString(), getIndent())).append("\n");
            }
        }
    }

    private void printFields(StringBuilder sb) {
        if (!fields.isEmpty()) {
            sb.append("//fields\n");
            if (Config.fields_public) {
                printFields(fields, "public:", sb);
            }
            else {
                List<CField> public_fields = fields.stream().filter(CField::isPublic).collect(Collectors.toList());
                List<CField> priv_fields = fields.stream().filter(CField::isPrivate).collect(Collectors.toList());
                printFields(public_fields, "public:", sb);
                printFields(priv_fields, "private:", sb);
            }

            sb.append("\n");
        }
    }

    private void printFields(List<CField> list, String modifier, StringBuilder sb) {
        if (list.size() > 0) {
            getScope(list);
            sb.append(modifier).append("\n");
            for (CField cf : list) {
                sb.append(getIndent()).append(cf).append("\n");
            }
        }
    }

    public void printDestructor() {
        //todo
        CMethod destructor = new CMethod();
        destructor.parent = this;
        destructor.name = new CName("~" + name);
        destructor.setVirtual(true);
        append("virtual ~").append(name).append("(){}");
    }

    Namespace getNs() {
        if (ns != null) {
            return ns;
        }
        if (parent != null) {
            //return parent.getNs();
            throw new RuntimeException("getNs deprecated code");
        }
        return null;
    }

    public CType getType() {
        if (type == null) {
            if (getNs() == null) {
                type = new CType(name);
            }
            else {
                type = new CType(getNs().all + "::" + name);
            }
            type.scope = header;
        }
        return type;
    }


}
