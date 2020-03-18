package com.mesut.j2cpp;


import com.mesut.j2cpp.ast.CClass;
import com.mesut.j2cpp.ast.CHeader;
import com.mesut.j2cpp.ast.CType;
import com.mesut.j2cpp.ast.Namespace;
import org.eclipse.jdt.core.dom.*;

import java.util.Stack;

public class HeaderWriter extends WriterVisitor {

    CHeader header;
    CompilationUnit unit;
    public Stack<CClass> stack = new Stack<>();

    public HeaderWriter(CompilationUnit unit) {
        this.unit = unit;
    }

    public void write() {
        //start visit
    }

    public CClass last() {
        return stack.peek();
    }

    @Override
    public boolean visit(PackageDeclaration node) {
        Namespace ns = new Namespace();
        ns.fromPkg(node.getName().getFullyQualifiedName());
        header.ns = ns;
        return true;
    }

    @Override
    public boolean visit(TypeDeclaration node) {
        CClass cc = new CClass();
        if (stack.size() == 0) {
            header.addClass(cc);
        }
        else {
            last().addInner(cc);
        }
        stack.push(cc);

        cc.name = node.getName().getFullyQualifiedName();
        cc.isInterface = node.isInterface();


        node.typeParameters().forEach(type -> cc.template.add(new CType(type.toString(), true)));

        if (node.getSuperclassType() != null) {
            CType baseType = typeVisitor.visitType(node.getSuperclassType(), cc);
            baseType.isTemplate = false;
            baseType.isPointer = false;
            cc.base.add(baseType);
        }


        node.superInterfaceTypes().forEach(iface -> {
            /*CType ifType = typeVisitor.visitType(iface, cc);
            ifType.isTemplate = false;
            ifType.isPointer = false;
            cc.base.add(ifType);*/
        });
        //inner classes
        for (TypeDeclaration member : node.getTypes()) {
            member.accept(this);
        }
        for (FieldDeclaration field : node.getFields()) {
            field.accept(this);
        }
        for (MethodDeclaration method : node.getMethods()) {
            method.accept(this);
        }
        stack.pop();
        return false;
    }
}
