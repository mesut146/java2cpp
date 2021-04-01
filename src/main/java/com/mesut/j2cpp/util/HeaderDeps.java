package com.mesut.j2cpp.util;

import com.mesut.j2cpp.ast.CHeader;
import com.mesut.j2cpp.ast.CType;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

public class HeaderDeps extends ASTVisitor {
    CHeader header;

    public void handle() {
        if (header.cc.getSuper() != null) {
            header.includes.add(header.cc.getSuper());
        }
        for (CType base : header.cc.ifaces) {
            header.includes.add(base);
        }
    }

    @Override
    public boolean visit(FieldDeclaration node) {
        for (int i = 0; i < node.fragments().size(); i++) {
            VariableDeclarationFragment fragment = (VariableDeclarationFragment) node.fragments().get(i);
            if (fragment.getInitializer() != null) {

            }
        }
        return super.visit(node);
    }
}
