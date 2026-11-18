package base;

import java.util.*;

public class Box{

  Boolean box(){
    Object o = 5;
    Integer j = (Integer) o;
    int x = j;
    List<Integer> l=new ArrayList<>();
    l.add(6);
    return x == 5;
  }

  boolean unbox(Integer i, Boolean b){
    int x = i;
    return b;
  }
}