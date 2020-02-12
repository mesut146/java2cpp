package base;

import java.util.*;

public class Generic<T, V> extends List<T> {

    public void add(T param) {
        T obj = new T();
        T[] arr = new T[10];
        add(arr[0]);
    }
}