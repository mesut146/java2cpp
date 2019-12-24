public class Generic<T,V> extends List<T>{

    void add(T elem){
        T obj=new T();
        T[] arr=new T[10];
        add(elem);
    }
}