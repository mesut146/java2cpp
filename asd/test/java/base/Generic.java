package base;

import java.util.*;

public class Generic<T, V> extends List<T> {

    public void add(T param) {
        T obj = null;
        T[] arr = new T[10];
        arr[0]=obj;
        add(arr[0]);
    }
}