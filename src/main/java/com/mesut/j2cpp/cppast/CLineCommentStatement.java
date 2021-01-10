package com.mesut.j2cpp.cppast;

public class CLineCommentStatement extends CStatement {
    public String value;

    public CLineCommentStatement(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "//" + value;
    }
}
