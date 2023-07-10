package com.mesut.j2cpp.visitor;

import com.mesut.j2cpp.Config;
import com.mesut.j2cpp.Util;
import com.mesut.j2cpp.ast.*;
import com.mesut.j2cpp.cppast.CExpression;
import com.mesut.j2cpp.cppast.CNode;
import com.mesut.j2cpp.cppast.RawStatement;
import com.mesut.j2cpp.cppast.expr.*;
import com.mesut.j2cpp.cppast.literal.CNumberLiteral;
import com.mesut.j2cpp.cppast.literal.CStringLiteral;
import com.mesut.j2cpp.cppast.stmt.CBlockStatement;
import com.mesut.j2cpp.cppast.stmt.CExpressionStatement;
import com.mesut.j2cpp.cppast.stmt.CReturnStatement;
import com.mesut.j2cpp.map.ClassMap;
import com.mesut.j2cpp.map.Mapper;
import com.mesut.j2cpp.util.ArrayHelper;
import com.mesut.j2cpp.util.DepVisitor;
import com.mesut.j2cpp.util.TypeHelper;
import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public class DeclarationVisitor {

    public boolean visitSource = true;
    public List<CClass> classes = new ArrayList<>();
    public Namespace ns;
    SourceVisitor sourceVisitor;

    public DeclarationVisitor(SourceVisitor sourceVisitor) {
        this.sourceVisitor = sourceVisitor;
    }
    

    public void convert(CompilationUnit cu) {
        ns = visit(cu.getPackage());

        for (AbstractTypeDeclaration decl : (List<AbstractTypeDeclaration>) cu.types()) {
            if (decl instanceof EnumDeclaration) {
                visit((EnumDeclaration) decl, null);
            }
            else if (decl instanceof AnnotationTypeDeclaration) {
                //visit((AnnotationTypeDeclaration) decl, null);
            }
            else {
                visit((TypeDeclaration) decl, null);
            }
        }
    }

    public Namespace visit(PackageDeclaration n) {
        Namespace ns = new Namespace();
        if (n != null) {
            ns = new Namespace(n.getName().getFullyQualifiedName());
        }
        return ns;
    }

    /*public CClass visit(AnnotationTypeDeclaration node, CClass outer) {
        CClass cc = new CClass();
        classes.add(cc);
        cc.isInterface = true;
        cc.ifaces.add(TypeHelper.getAnnotationType());
        cc.name = node.getName().getIdentifier();
        node.bodyDeclarations().forEach(body -> visitBody((BodyDeclaration) body, cc));
        return cc;
    }*/

    public CClass visit(TypeDeclaration node, CClass outer) {
        CClass cc = ClassMap.sourceMap.get(node.resolveBinding());
        classes.add(cc);
        node.bodyDeclarations().forEach(body -> visitBody((BodyDeclaration) body, cc));
        new DepVisitor(cc, node).handle();
        return cc;
    }

    private void visitBody(BodyDeclaration body, CClass cc) {
        if (body instanceof EnumDeclaration) {
            visit((EnumDeclaration) body, cc);
        }
        else if (body instanceof FieldDeclaration) {
            visit((FieldDeclaration) body, cc);
        }
        else if (body instanceof MethodDeclaration) {
            visit((MethodDeclaration) body, cc);
        }
        else if (body instanceof TypeDeclaration) {//inner class
            visit((TypeDeclaration) body, cc);
        }
        else if (body instanceof Initializer) {
            visit((Initializer) body, cc);
        }
        else {
            System.out.println("body type: " + body.getClass());
        }
    }

    CBlockStatement initEnum(CClass cc) {
        //add ordinal field
        CField ord = new CField();
        ord.type = new CType("int");
        ord.name = new CName("ordinal");
        cc.addField(ord);
        //enum cons name field
        CField name = new CField();
        name.type = Mapper.instance.mapType(TypeHelper.getStringType(), cc);
        name.name = new CName("cons_name");
        cc.addField(name);
        //init method for ordinal & cons name
        CMethod init = new CMethod();
        init.name = new CName("init_cons");
        init.type = TypeHelper.getVoidType();
        init.setStatic(true);
        init.body = new CBlockStatement();
        cc.addMethod(init);
        //static init
        mainEntrySiCall(init);
        //toString method
        CMethod toStr = new CMethod();
        toStr.name = new CName("toString");
        toStr.type = Mapper.instance.mapType(TypeHelper.getStringType(), cc);
        toStr.body = new CBlockStatement();
        toStr.body.addStatement(new CReturnStatement(name.name));
        cc.addMethod(toStr);
        //compareTo method
        CMethod cmp = new CMethod();
        cmp.name = new CName("compareTo");
        cmp.type = new CType("int");
        cmp.addParam(new CParameter(cc.getType(), CName.simple("other")));
        cmp.body = new CBlockStatement();
        cmp.body.addStatement(new CReturnStatement(new CInfixExpression(CName.simple("ordinal"), new CMethodInvocation(CName.simple("other"), CName.simple("ordinal"), true), "==")));
        cc.addMethod(cmp);
        return init.body;
    }

    public CClass visit(EnumDeclaration n, CClass outer) {
        CClass cc = ClassMap.sourceMap.get(n.resolveBinding());
        classes.add(cc);
        CBlockStatement block = initEnum(cc);
        int ordinal = 0;
        List<CField> consList = new ArrayList<>();
        for (EnumConstantDeclaration constant : (List<EnumConstantDeclaration>) n.enumConstants()) {
            CField field = PreVisitor.visitField(constant.resolveVariable(), cc);
            consList.add(field);
            CClassInstanceCreation rhs = new CClassInstanceCreation();
            field.expression = rhs;
            rhs.setType(field.type);

            if (!constant.arguments().isEmpty()) {
                rhs.args = sourceVisitor.list(constant.arguments());
            }
            if (constant.getAnonymousClassDeclaration() != null) {
                CClassInstanceCreation creation = new AnonyHandler().handle(constant.getAnonymousClassDeclaration(), cc.getType(), cc, sourceVisitor);
                rhs.setType(creation.type);
            }
            //add ordinal && name
            CFieldAccess name = new CFieldAccess(cc.getType(), field.name, false);
            block.addStatement(new CExpressionStatement(new CAssignment(new CFieldAccess(name, new CName("ordinal"), true), new CNumberLiteral(Integer.toBinaryString(ordinal)), "=")));
            block.addStatement(new CExpressionStatement(new CAssignment(new CFieldAccess(name, new CName("cons_name"), true), SourceVisitor.stringCreation(new CStringLiteral(field.name.name), cc), "=")));
            ordinal++;
        }
        //init values() method
        CMethod values = Util.getMethod(cc, "values");
        values.body = new CBlockStatement();
        CObjectCreation obj = new CObjectCreation();
        obj.type = TypeHelper.getVectorType();
        obj.type.typeNames.add(CType.asPtr(cc.getType()));
        obj.type.setPointer(false);
        CArrayInitializer arr = new CArrayInitializer();
        for (CField cons : consList) {
            arr.expressions.add(new CFieldAccess(new CFieldAccess(cc.getType(), cons.name, false), cons.name, true));
        }
        obj.args.add(arr);
        values.body.addStatement(new CReturnStatement(obj));

        //init valueOf method
        CMethod valueof = Util.getMethod(cc, "valueOf");
        valueof.params.get(0).name = new CName("name");
        String str = "for($type* e : *values()){\n" +
                "    if(e->cons_name == name){\n" +
                "        return e;\n" +
                "    }\n" +
                "}\n" +
                "return nullptr;";
        RawStatement raw = new RawStatement(str.replace("$type", cc.getType().toString()));
        valueof.body = new CBlockStatement();
        valueof.body.addStatement(raw);

        if (!n.bodyDeclarations().isEmpty()) {
            n.bodyDeclarations().forEach(p -> visitBody((BodyDeclaration) p, cc));
        }
        return cc;
    }

    public void visit(FieldDeclaration n, CClass clazz) {
        CType type = TypeVisitor.fromBinding(n.getType().resolveBinding(), clazz);

        for (VariableDeclarationFragment frag : (List<VariableDeclarationFragment>) n.fragments()) {
            CField field = PreVisitor.visitField(frag.resolveBinding(), clazz);
            if (clazz.isInterface) {
                field.setStatic(true);
            }
            if (frag.getExtraDimensions() > 0) {
                //c style array
                field.setType(ArrayHelper.makeArrayType(type, frag.getExtraDimensions()));
            }
            else {
                field.setType(type);
            }

            if (frag.getInitializer() != null) {
                sourceVisitor.clazz = clazz;
                sourceVisitor.binding = frag.resolveBinding().getDeclaringClass();
                field.expression = (CExpression) sourceVisitor.visitExpr(frag.getInitializer(), null);
                sourceVisitor.clazz = null;
                
            }
        }
    }

    public CNode visit(MethodDeclaration node, CClass clazz) {
        CMethod method = PreVisitor.visitMethod(node.resolveBinding(), clazz);
        //replace real names
        for (int i = 0; i < node.parameters().size(); i++) {
            SingleVariableDeclaration param = (SingleVariableDeclaration) node.parameters().get(i);
            CParameter cp = method.params.get(i);
            //todo check name
            cp.setName(CName.from(param.getName().getIdentifier()));
        }
        if (visitSource) {
            sourceVisitor.clazz = clazz;
            sourceVisitor.method = method;
            sourceVisitor.binding = node.resolveBinding().getDeclaringClass();
            method.body = (CBlockStatement) sourceVisitor.visitExpr(node.getBody(), null);
        }
        return method;
    }

    //make main entry call this method
    void mainEntrySiCall(CMethod method) {
        //main entry should call this
        CMethod si = getSiInit();
        CMethodInvocation call = new CMethodInvocation(method.parent.getType(), method.name, false);
        si.body.addStatement(new CExpressionStatement(call));
    }

    //static initializer
    public void visit(Initializer node, CClass cc) {
        CMethod method = new CMethod();
        cc.addMethod(method);
        method.name = CName.from(Config.static_init_name);
        method.type = TypeHelper.getVoidType();
        method.setStatic(true);
        sourceVisitor.method = method;
        sourceVisitor.clazz = cc;
        method.body = (CBlockStatement) sourceVisitor.visitExpr(node.getBody(), null);

        //main entry should call this
        mainEntrySiCall(method);
    }

    //get static initializer method init main entry
    CMethod getSiInit() {
        for (CMethod method : ClassMap.sourceMap.mainClass.methods) {
            if (method.name.is("si_init")) {
                return method;
            }
        }
        return null;//never happens
    }
}
