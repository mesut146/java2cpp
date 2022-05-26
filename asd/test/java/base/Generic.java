package base;

import java.util.*;

class type1<P> {
    <X> void add(P elem, X other) {
    }
}

class type2 {
}

class type3 extends type2{}


public class Generic<T, V> extends type1<T> {

    //<class T,class V,class X>
    public <X> void meth(T param, X other) {
        type1<T> obj = new type1<>();//auto type substition
        obj.add(param, other);
        add(param, other);
        List<String> l = null;
        String s = l.get(0);
    }

    void wild(List<? super type2> list) {
        list.add(new type3());
        list.add(new type2());
        Object t2 = list.get(0);
    }

    void wild2(List<? extends type2> list) {
        type2 t2 = list.get(0);
    }
}
