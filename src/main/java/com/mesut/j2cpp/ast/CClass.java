package com.mesut.j2cpp.ast;

import com.mesut.j2cpp.Config;
import com.mesut.j2cpp.IncludeList;
import com.mesut.j2cpp.Logger;
import com.mesut.j2cpp.cppast.CStatement;
import com.mesut.j2cpp.map.ClassMap;
import com.mesut.j2cpp.util.PrintHelper;
import com.mesut.j2cpp.util.TypeHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CClass extends CStatement {

    public String name;
    public boolean isInterface = false;
    public boolean isStatic = false;
    public boolean isPublic = false;
    public boolean isAnonymous = false;
    public boolean isInner = false;
    public Namespace ns = new Namespace();
    public CType superClass;
    public List<CType> ifaces = new ArrayList<>();
    public Template template = new Template();
    public List<CField> fields = new ArrayList<>();
    public List<CMethod> methods = new ArrayList<>();
    public CClass parent;//outer
    public CHeader header;
    public List<CStatement> consStatements = new ArrayList<>();
    public Set<CType> types = new HashSet<>();
    public boolean initialized = false;
    public boolean initMembers = false;
    public boolean fromSource = true;
    public IncludeList includes = new IncludeList();
    CType type;
    int anonyCount = 0;

    public CClass() {
        if (Config.baseClassObject) {
            superClass = TypeHelper.getObjectType();
        }
    }

    public CClass(CType type) {
        this();
        this.type = type;
        name = type.getName();
        ns = type.ns;
        fromSource = type.fromSource;
    }

    public String getHeaderPath() {
        return IncludeList.getHeaderPath(getType());
    }

    public void addMethod(CMethod cm) {
        cm.parent = this;
        methods.add(cm);
    }

    public void addField(CField cf) {
        cf.parent = this;
        fields.add(cf);
    }

    public String getAnonyName() {
        return name + "_" + anonyCount++;
    }

    //add reference type that this class use
    public void addType(CType type) {
        if (type == null) return;
        if (type.isTemplate ||
                type.equals(getType()) ||
                type.isPrim() ||
                type.isVoid() ||
                type.mapped) return;
        types.add(type);
    }

    public Template getTemplate() {
        return template;
    }

    public CType getSuper() {
        return superClass;
    }

    public void setSuper(CType type) {
        if (type.equals(TypeHelper.getObjectType())) {
            if (Config.baseClassObject) {
                superClass = type;
            }
        }
        else {
            superClass = type;
        }
    }

    public String forwardStr() {
        StringBuilder sb = new StringBuilder();
        if (template != null && !template.isEmpty()) {
            sb.append(template).append("\n");
        }
        sb.append("class ");
        sb.append(type.getName());
        sb.append(";");
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        printDecl(sb);
        sb.append("{\n");
        up();
        printFields(sb);
        printMethods(sb);
        sb.append("};//class ").append(name);
        return sb.toString();
    }

    public void printDecl(StringBuilder sb) {
        getScope(template);
        if (isInterface) {
            sb.append("/*interface*/\n");
        }
        if (!template.isEmpty()) {
            sb.append("\n").append(template).append("\n");
        }
        //class decl
        sb.append("class ").append(name);
        printBase(sb);
    }

    void printBase(StringBuilder sb) {
        List<CType> all = new ArrayList<>();
        if (superClass != null) {
            all.add(superClass);
        }
        all.addAll(ifaces);
        int size = all.size();
        all = all.stream().filter(t -> !t.mapped).collect(Collectors.toList());
        if (all.size() != size) {
            Logger.log(getType() + "these base types mapped and not printed -> " + all.stream().filter(t -> t.mapped).collect(Collectors.toList()));
        }
        if (!all.isEmpty()) {
            getScope(all);
            sb.append(": ");
            for (int i = 0; i < all.size(); i++) {
                sb.append("public ").append(all.get(i));
                if (i < all.size() - 1) {
                    sb.append(", ");
                }
            }
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

    public void handleBaseVirtuals() {
        //create missing pure virtuals inherited from base
        List<CType> all = new ArrayList<>(ifaces);
        if (superClass != null) all.add(superClass);

        all = all.stream().filter(type1 -> !type1.mapped).collect(Collectors.toList());
        for (CType t : all) {
            CClass cc = ClassMap.sourceMap.get(t);
            if (cc.isInterface) {
                for (CMethod method : cc.methods) {
                    if (!method.isStatic()) {
                        addMethod(method);
                    }
                }
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

    public CType getType() {
        if (type == null) {
            if (ns.isEmpty()) {
                type = new CType(name);
            }
            else {
                type = new CType(ns.all + "::" + name);
            }
            type.scope = header;//?
        }
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CClass other = (CClass) o;
        return getType().equals(other.getType());
    }

    @Override
    public int hashCode() {
        return getType().hashCode();
    }
}
