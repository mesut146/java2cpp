package com.mesut.j2cpp;
import com.mesut.j2cpp.ast.*;

public class CSource extends Node
{
    CHeader h;

    @Override
    public void print()
    {
        append("#include \"");
        append(h.getInclude());
        appendln("\"").println();
        
        for(CClass cc:h.classes){
            for(CMethod cm:cc.methods){
                line("");
                if(!cm.isCons){
                    append(cm.type).append(" ");
                }
                append(cc.name).
                append("::").
                append(cm.name).
                append("(");
                for(int i=0;i<cm.params.size();i++){
                    CParameter cp=cm.params.get(i);
                    append(cp.toString());
                    if(i<cm.params.size()-1){
                        append(",");
                    }
                }
                appendln("){");
                append(cm.body.toString());
                appendln("}").println();
            }
        }
    }
    
    
}
