package com.mesut.j2cpp.visitor;

import com.mesut.j2cpp.Converter;
import com.mesut.j2cpp.ast.CClass;
import com.mesut.j2cpp.ast.CMethod;
import com.mesut.j2cpp.ast.CSource;
import com.mesut.j2cpp.ast.Node;
import com.mesut.j2cpp.cppast.CFieldDef;
import com.mesut.j2cpp.cppast.CNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public class SourceVisitor extends GenericVisitor<CNode, CNode> {

    Converter converter;
    CSource source;
    TypeVisitor typeVisitor;

    public SourceVisitor(Converter converter, CSource source) {
        this.converter = converter;
        this.source = source;
        typeVisitor = new TypeVisitor(converter, source.header);
    }

    CClass getClazz() {
        for (CClass clazz : source.header.classes) {
            for () {

            }
        }
    }

    @Override
    public CNode visit(CompilationUnit node, CNode arg) {
        for (CClass clazz : source.header.classes) {
            for () {

            }
        }
        return null;
    }

    @Override
    public CNode visit(FieldDeclaration n, CNode arg) {
        CFieldDef fieldDef = new CFieldDef();

        return fieldDef;
    }

    @Override
    public CNode visit(MethodDeclaration n, CNode arg) {
        CMethod method = new CMethod();
        method.name = n.getName().getIdentifier();
        method.type = typeVisitor.visitType(n.getReturnType2(), );
        return method;
    }
}
