package mapper;

import java.util.*;

public class SetTest {

    void test() {
        Set<String> set = new HashSet<>();
        String hello = "hello";
        set.add(hello);
        boolean b = set.contains(hello);
        int len = set.size();
        set.clear();
        boolean any = set.isEmpty();
        set.remove(hello);
    }
}
