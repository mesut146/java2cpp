package com.mesut.j2cpp.ast;

import java.util.ArrayList;
import java.util.List;

public abstract class Node {
    public String indention = "";
    public boolean useTab = false;
    public int level = 0;
    public List<String> list = new ArrayList<>();
    boolean isPrinted = false;
    public boolean firstBlock = false;
    public String cache = null;

    public abstract void print();

    public void init() {
        indention = "";
        String str = getIndent();
        for (int i = 0; i < level; i++) {
            indention = indention + str;
        }
    }

    String getIndent() {
        return useTab ? "\t" : "    ";
    }

    public Node line(String str) {
        //write(indention).write(indention).write(str);
        list.add(indention + str);
        return this;
    }

    public Node lineln(String str) {
        //line(str).println();
        line(str);
        println();
        return this;
    }

    public Node lineup(String s) {
        line(getIndent());
        line(s);
        return this;
    }

    public void clear(){
        list.clear();
    }

    public void println() {
        //write("\n");
        list.add("");
    }

    public Node append(CType type) {
        append(type.toString());
        return this;
    }

    public Node append(String str) {
        //write(indention).write(str);

        if (list.size() == 0) {
            list.add(indention + str);
        } else {
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

    public Node appendln(String str) {
        append(str).println();
        return this;
    }

    //no indention
    public Node append(Node n) {
        n.print();
        boolean flag = true;
        for (String s : n.list) {
            if (flag && n.firstBlock) {
                flag = false;
                append(s);
            } else {
                list.add(s);
            }
        }
        return this;
    }

    //append with indention
    public Node appendIndent(Node n) {
        n.print();
        boolean flag = true;
        for (String s : n.list) {
            if (flag && n.firstBlock) {
                flag = false;
                append(s);
            } else {
                list.add(indention + s);
            }
        }
        return this;
    }

    public Node include(String file) {
        appendln("#include \"" + file + ".h\"");
        return this;
    }

    public Node includePath(String file) {
        appendln("#include \"" + file + "\"");
        return this;
    }

    public void print_using(Namespace ns) {
        appendln("using namespace " + ns.getAll() + ";");
    }

    public void setTo(Node n) {
        n.level = this.level;
        n.init();
    }

    public void setFrom(Node n) {
        this.level = n.level;
        this.init();
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

    @Override
    public String toString() {
        if (cache != null) {
            return cache;
        }
        print();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i));
            if (i < list.size() - 1) {
                sb.append("\n");
            }
        }
        return cache = sb.toString();
    }


}
