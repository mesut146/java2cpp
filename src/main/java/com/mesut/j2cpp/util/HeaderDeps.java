package com.mesut.j2cpp.util;

import com.mesut.j2cpp.ast.CClass;
import com.mesut.j2cpp.ast.CHeader;
import com.mesut.j2cpp.ast.CType;
import com.mesut.j2cpp.map.ClassMap;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import java.util.ArrayList;
import java.util.List;

public class HeaderDeps{
    CHeader header;

    public HeaderDeps(CHeader header) {
        this.header = header;
    }

    public void handle() {
        List<CClass> list = new ArrayList<>();
        if (header.cc.getSuper() != null) {
            list.add(ClassMap.sourceMap.get(header.cc.getSuper()));
        }
        for (CType base : header.cc.ifaces) {
            list.add(ClassMap.sourceMap.get(base));
        }
        try {
            BaseClassSorter.sort(list);
            for (CClass cc : list) {
                header.includes.add(cc.getType());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*@Override
    public boolean visit(FieldDeclaration node) {
        for (int i = 0; i < node.fragments().size(); i++) {
            VariableDeclarationFragment fragment = (VariableDeclarationFragment) node.fragments().get(i);
            if (!Modifier.isStatic(node.getModifiers()) && fragment.getInitializer() != null) {
                //
            }
        }
        return super.visit(node);
    }*/
}
