package com.mesut.j2cpp.visitor;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SwitchInfo {
    SwitchStatement node;
    Expression expression;
    Map<Case, List<Statement>> map = new HashMap<>();
    List<Statement> def = new ArrayList<>();

    public SwitchInfo(SwitchStatement node) {
        this.node = node;
    }

    @SuppressWarnings("unchecked")
    public void make() {
        Case cas = new Case();
        for (var st : (List<Statement>) node.statements()) {
            if (st instanceof SwitchCase) {
                SwitchCase switchCase = (SwitchCase) st;
                if (switchCase.isDefault()) {

                }
                else {

                }
            }
            else {

            }
        }
    }

    public static class Case {
        List<Expression> list = new ArrayList<>();
    }

}
