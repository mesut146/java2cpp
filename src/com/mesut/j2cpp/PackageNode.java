package com.mesut.j2cpp;
import java.util.*;

public class PackageNode
{
    String name;
    List<PackageNode> nodes=new ArrayList<>();
    
    public PackageNode(String name){
        this.name=name;
    }
    
    public PackageNode addSub(String sub){
        PackageNode subnode=new PackageNode(sub);
        nodes.add(subnode);
        return subnode;
    }

    @Override
    public String toString()
    {
        if(nodes.size()==0){
            return name;
        }
        StringBuffer sb=new StringBuffer();
        for(PackageNode pn:nodes){
            sb.append(name);
            sb.append("->");
            sb.append(pn.toString());
            sb.append("\n");
        }
        return sb.toString();
    }
    
   
}
