package mapper;

import java.util.*;

public class ListTest{

    public static void test(){
        List<String> list = new ArrayList<>();
        list.add("hello");
        list.add("world");

        String s1 = list.get(0);
        list.set(1, s1);
    }

}