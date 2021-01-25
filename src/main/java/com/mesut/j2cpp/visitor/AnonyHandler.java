package com.mesut.j2cpp.visitor;

import com.mesut.j2cpp.ast.CClass;
import com.mesut.j2cpp.ast.CMethod;
import com.mesut.j2cpp.ast.CType;
import com.mesut.j2cpp.cppast.CClassImpl;
import com.mesut.j2cpp.cppast.expr.CClassInstanceCreation;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import java.net.Socket;
import java.util.List;

public class AnonyHandler {

    //make separate class def for anonymous class
    public static CClassInstanceCreation handle(AnonymousClassDeclaration declaration, CType type, CClass clazz, SourceVisitor visitor) {
        CClass anony = new CClass();
        anony.header = clazz.header;
        anony.parent = clazz;
        anony.isAnonymous = true;
        anony.name = clazz.header.getAnonyName();
        anony.ns = clazz.ns;
        anony.base.add(type);

        SourceVisitor newVisitor = new SourceVisitor(visitor.source);
        visitor.typeVisitor.fromBinding(declaration.resolveBinding());
        //newVisitor.binding = declaration.resolveBinding();

        DeclarationVisitor declarationVisitor = new DeclarationVisitor(newVisitor);
        for (BodyDeclaration body : (List<BodyDeclaration>) declaration.bodyDeclarations()) {
            if (body instanceof FieldDeclaration) {
                declarationVisitor.visit((FieldDeclaration) body, anony);
            }
            else if (body instanceof MethodDeclaration) {
                declarationVisitor.visit((MethodDeclaration) body, anony);
            }
            else {
                throw new RuntimeException("ClassInstanceCreation anony");
            }
        }
        InnerHelper.handleRef(anony, clazz);

        CClassInstanceCreation creation = new CClassInstanceCreation();
        creation.setType(new CType(anony.name));
        clazz.header.source.anony.add(new CClassImpl(anony));
        return creation;
    }
}
