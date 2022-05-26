package com.mesut.j2cpp.visitor;

import org.eclipse.jdt.core.dom.*;
import com.mesut.j2cpp.ast.*;
import com.mesut.j2cpp.*;
import com.mesut.j2cpp.util.*;

import java.util.*;

public class Code{
	StringBuilder sb = new StringBuilder();
	int level = 0;
	String indent="";
	List<String> usings = new ArrayList<>();
	
	void init(){
		indent ="";
		for(int i=0;i<level;i++){
			indent=indent+"    ";
		}
	}
	
	public void up(){
		level++;
		init();
	}
	
	public void down(){
		level--;
		init();
	}
	
	public void clear(){
		sb.setLength(0);
	}
	
	String str(ITypeBinding b){
		if(b.getName().equals("void")) return"void";
		String s;
		if(b.isTypeVariable()){
            s = TypeHelper.getObjectType().toString();
        }
        else{
		CType ct = TypeVisitor.fromBinding(b);
		if(Config.full) ct.typeNames.clear();
	    s = ct.toString();
		for(String u : usings){
			if(s.startsWith(u)){
				return s.substring(u.length() + 2);
		    }
        }
        }
        return s;
    }
	
	public void write(ITypeBinding b){
		write(str(b));
    }
	
	public void write(String s, Object...args){
		if(args.length != 0){
			for(int i = 0;i < args.length;i++){
				if(args[i] instanceof ITypeBinding){
					ITypeBinding type = (ITypeBinding)args[i];
					args[i] = str(type);
			    }
		    }
            s = String.format(s, args);
        }
		sb.append(s);
	}
	
	public void line(String s, Object...args){
		sb.append(indent);
		write(s, args);
	}
	
	public String ptr(ITypeBinding b){
         String s = str(b);
         if(!b.isPrimitive()){
             s = s + "*";
         }
         return s;
     }
     
     public String ptr(CType b){
         String s = b.toString();
         s = s + "*";
         return s;
     }
	
	public String toString(){
		return sb.toString();
	}

}