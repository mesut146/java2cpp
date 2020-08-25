package com.mesut.j2cpp.visitor;

import com.mesut.j2cpp.cppast.CNode;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;

public interface Catcher {

    CNode visit(ReturnStatement node);

    CNode visit(ThrowStatement node);

}
