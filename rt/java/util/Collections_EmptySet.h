#pragma once


namespace java{
namespace util{


template <typename E>
class EmptySet: public AbstractSet<E>, public java::io::Serializable{
//fields
public:
    static long serialVersionUID;

//methods
public:
    EmptySet();

    bool contains(java::lang::Object* );

    bool containsAll(Collection<java::lang::Object*>* );

    void forEach(java::util::function::Consumer<java::lang::Object*>* );

    bool isEmpty();

    Iterator<E>* iterator();

    java::lang::Object* readResolve();

    bool removeIf(java::util::function::Predicate<java::lang::Object*>* );

    int size();

    Spliterator<E>* spliterator();

    std::vector<java::lang::Object*>* toArray();

    template <typename T>
    std::vector<T>* toArray(std::vector<T>* );


};//class EmptySet

}//namespace java
}//namespace util
