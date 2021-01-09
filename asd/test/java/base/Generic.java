package base;

import java.util.*;

public class Generic<T, V> extends ArrayList<T> {

    public void meth(T param) {
        T obj = null;
        List<T> arr = new ArrayList<>();
        arr.add(obj);
        add(arr.get(0));
    }

    List<String> add() {
        T obj = (T) new Object();
        add(obj);
        super.add(obj);
        return null;
    }
}
