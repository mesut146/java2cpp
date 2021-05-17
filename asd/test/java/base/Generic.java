package base;

import java.util.*;

class type<P> {
    <X> void add(P elem, X other) {
    }

    //public class mm{}
}

class type2 {
}


public class Generic<T, V> extends type<T> {

    //<class T,class V,class X>
    public <X> void meth(T param, X other) {
        type<T> obj = new type<>();//auto type substition
        obj.add(param, other);
        add(param, other);
    }

    void wild(List<? extends type2> list) {
        list.add(new type2());
    }
}
