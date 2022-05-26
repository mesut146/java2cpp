#pragma once


namespace java{
namespace util{


template <typename E>
class SynchronizedCollection: public Collection<E>, public java::io::Serializable{
//fields
public:
    Collection<E>* c;
    java::lang::Object* mutex;
    static long serialVersionUID;

//methods
public:
    SynchronizedCollection(Collection<E>* );

    SynchronizedCollection(Collection<E>* , java::lang::Object* );

    bool add(E );

    bool addAll(Collection<java::lang::Object*>* );

    void clear();

    bool contains(java::lang::Object* );

    bool containsAll(Collection<java::lang::Object*>* );

    void forEach(java::util::function::Consumer<java::lang::Object*>* );

    bool isEmpty();

    Iterator<E>* iterator();

    java::util::stream::Stream<E>* parallelStream();

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

    void writeObject(java::io::ObjectOutputStream* );


};//class SynchronizedCollection

}//namespace java
}//namespace util
