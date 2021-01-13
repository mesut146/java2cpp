package com.mesut.j2cpp.ast;

import java.util.ArrayList;
import java.util.List;

public abstract class Node {
    public String indention = "";
    public int level = 0;
    public boolean useTab = false;
    public List<String> list = new ArrayList<>();//list of lines
    public Object scope;

    public void print() {
    }

    public void init() {
        indention = "";
        String str = useTab ? "\t" : "    ";
        for (int i = 0; i < level; i++) {
            indention = indention + str;
        }
    }

    public String getIndent() {
        return indention;
    }

    public Node line(String str) {
        list.add(indention + str);
        return this;
    }


    public Node append(String str) {
        if (list.size() == 0) {
            list.add(indention + str);
        }
        else {
            int idx = list.size() - 1;
            String last = list.get(idx);
            if (last.length() == 0) {
                str = indention + str;
            }
            list.remove(idx);
            list.add(last + str);
            //list.set(idx,last+str);
        }
        return this;
    }

    //no indention
    public Node append(Node node) {
        node.scope = scope;
        node.ensurePrint();
        list.addAll(node.list);
        return this;
    }

    void ensurePrint() {
        print();
    }

    //append with indention
    public Node appendIndent(Node node) {
        node.scope = scope;
        node.ensurePrint();
        for (String line : node.list) {
            list.add(indention + line);
        }
        return this;
    }

    public void up() {
        level++;
        init();
    }

    public void down() {
        level--;
        if (level < 0) level = 0;
        init();
    }

    public void getScope(Node... arr) {
        for (Node node : arr) {
            if (node != null)
                node.scope = scope;
        }
    }

    @SafeVarargs
    public final <T extends Node> void getScope(List<T>... arr) {
        for (List<T> list : arr) {
            for (T t : list) {
                if (t != null)
                    t.scope = scope;
            }
        }
    }

    /*@SafeVarargs
    public final void getScope(Object... arr) {
        for (Object list : arr) {
            if (list instanceof List) {
                getScope((List<? extends Node>) list);
            }
            else if (list != null) {
                ((Node) list).scope = scope;
            }
        }
    }*/
}
