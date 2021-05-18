package com.mesut.j2cpp.visitor;

import com.mesut.j2cpp.ast.CClass;
import com.mesut.j2cpp.ast.CType;
import com.mesut.j2cpp.cppast.CClassImpl;
import com.mesut.j2cpp.cppast.expr.CClassInstanceCreation;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import java.util.List;

public class AnonyHandler {
    CClass anony;

    //make separate class def for anonymous class
    public CClassInstanceCreation handle(AnonymousClassDeclaration declaration, CType base, CClass clazz, SourceVisitor visitor) {
        anony = new CClass();
        anony.header = clazz.header;
        anony.parent = clazz;
        anony.isAnonymous = true;
        anony.name = clazz.getAnonyName();
        anony.ns = clazz.ns;
        anony.setSuper(base);

        SourceVisitor newVisitor = new SourceVisitor(visitor.source);
        TypeVisitor.fromBinding(declaration.resolveBinding());
        newVisitor.binding = declaration.resolveBinding();

        DeclarationVisitor declarationVisitor = new DeclarationVisitor(newVisitor);
        for (BodyDeclaration body : (List<BodyDeclaration>) declaration.bodyDeclarations()) {
            if (body instanceof FieldDeclaration) {
                declarationVisitor.visit((FieldDeclaration) body, anony);
            }
            else if (body instanceof MethodDeclaration) {
                declarationVisitor.visit((MethodDeclaration) body, anony);
            }
            else {
                //inner of anony?
                throw new RuntimeException("ClassInstanceCreation of anony");
            }
        }
        InnerHelper.handleRef(anony, clazz);

        CClassInstanceCreation creation = new CClassInstanceCreation();
        creation.setType(new CType(anony.name));
        visitor.source.anony.add(new CClassImpl(anony));
        return creation;
    }
}
