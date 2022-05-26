#pragma once


namespace java{
namespace util{


template <typename E>
class AsLIFOQueue: public AbstractQueue<E>, public Queue<E>, public java::io::Serializable{
//fields
public:
    Deque<E>* q;
    static long serialVersionUID;

//methods
public:
    AsLIFOQueue(Deque<E>* );

    bool add(E );

    void clear();

    bool contains(java::lang::Object* );

    bool containsAll(Collection<java::lang::Object*>* );

    E element();

    void forEach(java::util::function::Consumer<java::lang::Object*>* );

    bool isEmpty();

    Iterator<E>* iterator();

    bool offer(E );

    java::util::stream::Stream<E>* parallelStream();

    E peek();

    E poll();

    E remove();

    bool remove(java::lang::Object* );

    bool removeAll(Collection<java::lang::Object*>* );

    bool removeIf(java::util::function::Predicate<java::lang::Object*>* );

    bool retainAll(Collection<java::lang::Object*>* );

    int size();

    Spliterator<E>* spliterator();

    java::util::stream::Stream<E>* stream();

    std::vector<java::lang::Object*>* toArray();

    template <typename T>
    std::vector<T>* toArray(std::vector<T>* );

    std::string* toString();


};//class AsLIFOQueue

}//namespace java
}//namespace util
