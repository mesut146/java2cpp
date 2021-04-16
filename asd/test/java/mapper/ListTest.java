package mapper;

import java.util.*;

public class ListTest{

    public static void test(){
        List<String> list = new ArrayList<>();
        list.add("hello");
        list.add("world");

        String s1 = list.get(0);
        String foo = list.set(1, s1);
        String[] arr = (String[]) list.toArray();
        String[] arr2 = list.toArray(new String[0]);
    }

}