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
        append("\"\n");
        
        for(CClass cc:h.classes){
            for(CMethod cm:cc.methods){
                print("");
                if(!cm.isCons){
                    append(cm.type);
                    append(" ");
                }
                append(cc.name).append("::");
                append(cm.name);
                append("(");
                for(int i=0;i<cm.params.size();i++){
                    CParameter cp=cm.params.get(i);
                    cp.pw=pw;
                    cp.print();
                    if(i<cm.params.size()-1){
                        append(",");
                    }
                }
                append("){\n");

                append("}\n");
            }
        }
    }
    
    
}
